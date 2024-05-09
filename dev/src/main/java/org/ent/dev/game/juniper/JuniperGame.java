package org.ent.dev.game.juniper;

import org.ent.Ent;
import org.ent.net.Net;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.accessor.Accessors;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.net.node.cmd.split.Conditions;
import org.ent.permission.Permissions;
import org.ent.permission.WriteFacet;
import org.ent.run.EntRunner;
import org.ent.run.StepResult;
import org.ent.util.Logging;
import org.ent.util.builder.*;
import org.ent.webui.WebUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ent.dev.game.juniper.HabitatMap.MAP_TINY;
import static org.ent.net.node.cmd.operation.Operations.SET_OPERATION;
import static org.ent.util.builder.ExternalNode.external;
import static org.ent.util.builder.NodeTemplate.NOT;
import static org.ent.util.builder.NodeTemplate.node;

public class JuniperGame {

    private final static Logger log = LoggerFactory.getLogger(JuniperGame.class);

    private static final boolean WEB_UI = true;

    private final HabitatMap map;
    private final Ent ent;
    private Net habitat;

    public JuniperGame(HabitatMap map) {
        this.map = map;
        this.ent = new Ent(buildMainNet());
    }

    public Ent getEnt() {
        return ent;
    }

    public Net getHabitat() {
        return habitat;
    }

    private static Net buildMainNet() {
        Net net = new Net();
        net.newRoot(Permissions.DIRECT);
        return net;
    }

    public static void main(String[] args) {
        if (WEB_UI) {
            WebUI.setUpJavalin();
        }
        HabitatMap map = new HabitatMap(MAP_TINY);
        JuniperGame game = new JuniperGame(map);

        game.buildHabitat();
        game.map.dump();

        game.tryIt();
    }

    public void buildHabitat() {
        habitat = new Net();
        habitat.setName("habitat");
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                Node node = habitat.newNode(Permissions.DIRECT);
                node.setName("f_%s_%s".formatted(x, y));
                map.getField(x, y).node = node;
            }
        }
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                Node node = map.getField(x, y).node;
                node.setLeftChild(createNeighborsList(habitat, x, y), Permissions.DIRECT);
            }
        }
        habitat.setRoot(map.getField(0, 0).node);
        ent.addDomain(habitat);
    }

    private Node createNeighborsList(Net net, int x, int y) {
        Node list = null;
        for (Direction dir : Direction.valuesReversed()) {
            int xn = x + dir.deltaX();
            int yn = y + dir.deltaY();
            if (map.isXOutOfBounds(xn)) {
                continue;
            }
            if (map.isYOutOfBounds(yn)) {
                continue;
            }
            Node nodeNeighbor = map.getField(xn, yn).node;
            if (list == null) {
                list = net.newUNode(nodeNeighbor, Permissions.DIRECT);
            } else {
                list = net.newNode(nodeNeighbor, list, Permissions.DIRECT);
            }
            list.setValue(dir.label(), Permissions.DIRECT);
        }
        return list;
    }

    private void tryIt() {
        Net locationSetterNet = buildLocationSetterNet();
        ent.addDomain(locationSetterNet);

        Field field = map.getField(1, 0);
        Net locatorNet = buildLocatorMechanism(field.node(), locationSetterNet.getRoot());
        ent.addDomain(locatorNet);

        Node root = ent.getNet().getRoot();
        root.setValue(Commands.get(Operations.EVAL_FLOW_OPERATION, Accessors.L).getValue(), Permissions.DIRECT);
        root.setLeftChild(locatorNet.getRoot(), Permissions.DIRECT);

        ent.putPermissions(pb -> pb
                .net(p -> p
                        .canExecute(locatorNet)
                        .canWrite(locationSetterNet, WriteFacet.ARROW))
                .domain(locatorNet, p -> p
                        .canPointTo(habitat)
                        .canPointTo(locationSetterNet)
                        .canWrite(locationSetterNet, WriteFacet.ARROW))
                .domain(locationSetterNet, p -> p
                        .canPointTo(locatorNet))
        );

        Logging.logDot(ent);

        EntRunner runner = new EntRunner(ent);
        for (int i = 0; i < 30; i++) {
            StepResult stepResult = runner.step();
            log.info("step result {}: {}", i, stepResult);
            log.info("after step no. {}:", i);
            Logging.logDot(ent);
            if (stepResult == StepResult.CONCLUDED) {
                break;
            }
        }
    }

    public Net buildLocatorMechanism(Node initialLocation, Node locationSetter) {
        EntBuilder builder = new EntBuilder();
        var i = node().name("i");
        var l = node().name("location").left(external(initialLocation)).right(external(locationSetter));
        var root = node().setRoot();
        var n0 = node();
        var n1 = node();
        var n2 = node();
        var exit_success = node().name("exit_success");
        var exit_fail = node().name("exit_failure");

        builder.chain(
                // initialize i
                root.command(c -> c.operation(SET_OPERATION).argument1(i, Arg1.L).argument2(l, Arg2.LL)),
                n0.split(Conditions.IDENTICAL_CONDITION, Accessors.LLLL, Accessors.LRRL)
                        .name("is_found")
                        .left(node().left(i).right(l))
                        .right(node()
                                .left(exit_success)),
                n1.split(Conditions.IDENTICAL_CONDITION, Accessors.LLR, Accessors.LL)
                        .name("is_end_of_list")
                        .left(i)
                        .right(node()
                                .left(exit_fail)),
                n2.name("inc_i").command(c -> c.operation(SET_OPERATION).argument1(i, ArgSingle.L).argument2(i, ArgSingle.LR)),
                // goto top of loop
                n0
        );
        builder.chain(
                exit_fail.command(Commands.CONCLUSION_FAILURE),
                root
        );
        builder.chain(
                exit_success.name("set_new_location").command(c -> c.
                        operation(SET_OPERATION).
                        argument1(l, Arg1.L).
                        argument2(external(locationSetter), Arg2.L)),
                node().command(Commands.CONCLUSION_SUCCESS),
                root
        );
        Net locatorNet = builder.build();
        locatorNet.setName("locator");
        return locatorNet;
    }

    public Net buildLocationSetterNet() {
        Net locationSetterNet = new EntBuilder().build(
                node().setRoot().left(external(map.getField(0, 0).node())));
        locationSetterNet.setName("location_setter");
        ent.addDomain(locationSetterNet);
        return locationSetterNet;
    }

    private Net growerMachine() {
        EntBuilder builder = new EntBuilder();
        var n = node();
        var start = node();
        var n_div_2 = node();
        var c1 = node().value(1);
        var c1000 = node().value(1000);
        var limitExceeded = node();
        builder.chain(
                start.split(Conditions.GREATER_THAN_CONDITION, NOT, c1000, n)
                        .name("is_n_greater_or_equal_1000")
                        .right(node()
                                .left(limitExceeded)),
                node().command(Operations.SHIFT_LEFT_OPERATION, n_div_2, n, c1),
                node().command(Operations.PLUS_OPERATION, n, n, n_div_2),
                node().command(Operations.PLUS_OPERATION, n, n, c1)
        );
        builder.chain(
                limitExceeded.command(Operations.SET_VALUE_OPERATION, n, c1000),
                start
        );

        return builder.build();
    }
}
