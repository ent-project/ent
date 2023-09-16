package org.ent.net.util;

import org.ent.permission.Permissions;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.node.Node;

/**
 * Copy a Net.
 * Input net must be dense, i.e. no null values in Node array.
 */
public class NetCopy2 {

    private NetCopy2() {
    }

    public static Net createCopy(Net netOrig) {
        if (netOrig.isSparse()) {
            throw new IllegalArgumentException();
        }
        Net netClone = new Net();
        for (Node nodeOrig : netOrig.getNodes()) {
            netClone.newNode(nodeOrig.getValue());
        }
        netClone.setRoot(netClone.getNode(netOrig.getRoot().getIndex()));
        for (Node nodeOrig : netOrig.getNodes()) {
            Node nodeClone = netClone.getNode(nodeOrig.getIndex());
            for (ArrowDirection direction : ArrowDirection.values()) {
                Node targetOrig = nodeOrig.getChild(direction, Permissions.DIRECT);
                nodeClone.setChild(direction, netClone.getNode(targetOrig.getIndex()), Permissions.DIRECT);
            }
        }
        if (netOrig.getAnnotations() != null) {
            for (Node nodeOrig : netOrig.getAnnotations().keySet()) {
                Node nodeClone = netClone.getNode(nodeOrig.getIndex());
                netClone.setAnnotation(nodeClone, netOrig.getAnnotation(nodeOrig));
            }
        }
        return netClone;
    }
}
