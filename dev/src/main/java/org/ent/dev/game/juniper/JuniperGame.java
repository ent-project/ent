package org.ent.dev.game.juniper;

import org.ent.Ent;
import org.ent.net.Net;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.accessor.Accessors;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.net.node.cmd.veto.Conditions;
import org.ent.permission.Permissions;
import org.ent.permission.WriteFacet;
import org.ent.run.EntRunner;
import org.ent.run.StepResult;
import org.ent.util.Logging;
import org.ent.util.builder.Arg1;
import org.ent.util.builder.Arg2;
import org.ent.util.builder.ArgSingle;
import org.ent.util.builder.EntBuilder;
import org.ent.webui.WebUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.ent.net.node.cmd.operation.Operations.SET_OPERATION;
import static org.ent.util.builder.EntBuilder.*;

public class JuniperGame {

    private final static Logger log = LoggerFactory.getLogger(JuniperGame.class);

    private static final boolean WEB_UI = true;

    private static final String MAP = """
        #                  #
        #                  #
        #    J          S  #
        #            J     #
        #                  #
        #          S       #
        #                  #
        #  S               #
        #        J      J  #
        #                  #
        #                  #""";

    private static final String MAP1 = """
        #    J     S  #
        #       J     #
        #             #""";

    private static final String MAP_TINY = """
        #J S#
        #SJ #""";


    private final HabitatMap map;
    private Ent ent;
    private Net habitat;

    public JuniperGame(HabitatMap map) {
        this.map = map;
        this.ent = new Ent(buildMainNet());
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
                node.setName("(%s/%s)".formatted(x, y));
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
        Net locationSetterNet;
        try (EntBuilder builder = new EntBuilder()) {
            node().setRoot().left(external(map.getField(0,0).node));
            locationSetterNet = builder.build();
        }
        locationSetterNet.setName("location_setter");
        ent.addDomain(locationSetterNet);

        Field field = map.getField(1, 0);
        Net locatorNet = locatorMachine(field.node, locationSetterNet.getRoot());
        locatorNet.setName("locator");
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
                .domain(locationSetterNet, p-> p
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

    public Net locatorMachine(Node initialLocation, Node locationSetter) {
        try (EntBuilder entBuilder = new EntBuilder()) {
            var i = node().name("i");
            var l = node().name("location").left(external(initialLocation)).right(external(locationSetter));
            var root = node().setRoot();
            var n0 = node();
            var n1 = node();
            var n2 = node();
            var exit_success = node().name("exit_success");
            var exit_fail = node().name("exit_failure");

            chain(
                    // initialize i
                    root.command(c -> c.operation(SET_OPERATION).argument1(i, Arg1.L).argument2(l, Arg2.LL)),
                    n0.command(SET_OPERATION, Accessors.R, Accessors.LR)
                            .left(node().name("found?")
                                    .veto(Conditions.IDENTICAL_CONDITION, Accessors.LLLL, Accessors.LRRL)
                                    .left(node().left(i).right(l))
                                    .right(exit_success)),
                    n1.command(SET_OPERATION, Accessors.R, Accessors.LR)
                            .left(node().name("end of list?")
                                    .veto(Conditions.IDENTICAL_CONDITION, Accessors.LLR, Accessors.LL)
                                    .left(i)
                                    .right(exit_fail)),
                    n2.name("i++").command(c -> c.operation(SET_OPERATION).argument1(i, ArgSingle.L).argument2(i, ArgSingle.LR)),
                    // goto top of loop
                    n0
            );
            chain(
                    // restore condition pointer
                    exit_fail.command(c -> c.operation(SET_OPERATION).argument1(n1, Arg1.R).argument2(n2, Arg2.D)),
                    // collapse location setter
                    node().command(c -> c.operation(SET_OPERATION)
                            .argument1(external(locationSetter), ArgSingle.L)
                            .argument2(external(locationSetter), ArgSingle.D)),
                    node().command(Commands.CONCLUSION_FAILURE),
                    root
            );
            chain(
                    // restore condition pointer
                    exit_success.command(c -> c.operation(SET_OPERATION).argument1(n0, Arg1.R).argument2(n1, Arg2.D)),
                    node().name("set new location").command(c -> c.operation(SET_OPERATION).argument1(l, Arg1.L).argument2(external(locationSetter), Arg2.L)),
                    // collapse location setter
                    node().command(c -> c.operation(SET_OPERATION)
                            .argument1(external(locationSetter), ArgSingle.L)
                            .argument2(external(locationSetter), ArgSingle.D)),
                    node().command(Commands.CONCLUSION_SUCCESS),
                    root
            );
            return entBuilder.build();
        }
    }
}
