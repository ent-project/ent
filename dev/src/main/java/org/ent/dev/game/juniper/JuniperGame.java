package org.ent.dev.game.juniper;

import org.ent.Ent;
import org.ent.net.Net;
import org.ent.net.node.Node;
import org.ent.permission.Permissions;
import org.ent.util.Logging;
import org.ent.webui.WebUI;

public class JuniperGame {

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
        HabitatMap map = new HabitatMap(MAP1);
        JuniperGame game = new JuniperGame(map);

        game.buildHabitat();
        game.map.dump();

        Logging.logDot(game.ent);
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
}
