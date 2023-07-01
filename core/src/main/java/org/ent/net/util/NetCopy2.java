package org.ent.net.util;

import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;

public class NetCopy2 {

    private NetCopy2() {
    }

    public static Net createCopy(Net netOrig) {
        Net netClone = new Net();
        for (Node nodeOrig : netOrig.getNodes()) {
            netClone.newNode(nodeOrig.getValue(Purview.DIRECT));
        }
        netClone.setRoot(netClone.getNode(netOrig.getRoot().getIndex()));
        for (Node nodeOrig : netOrig.getNodes()) {
            Node nodeClone = netClone.getNode(nodeOrig.getIndex());
            for (ArrowDirection direction : ArrowDirection.values()) {
                Node targetOrig = nodeOrig.getChild(direction, Purview.DIRECT);
                nodeClone.setChild(direction, netClone.getNode(targetOrig.getIndex()), Purview.DIRECT);
            }
        }
        return netClone;
    }
}
