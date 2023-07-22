package org.ent.net.util;

import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;

/**
 * Copy a Net.
 *
 * Handles null values in the original Net.
 * Output Net will be packed (no null value in nodes array).
 */
public class NetCopyPack {
    private final Net netOrig;
    private final int[] indexMap;

    private Net netClone = new Net();


    public NetCopyPack(Net net) {
        this.netOrig = net;
        this.indexMap = new int[netOrig.getNodes().size()];
    }

    public Net createPackedCopy() {
        netClone = new Net();
        for (Node nodeOrig : netOrig.getNodes()) {
            if (nodeOrig == null) {
                continue;
            }
            Node newNode = netClone.newNode(nodeOrig.getValue(Purview.DIRECT));
            indexMap[nodeOrig.getIndex()] = newNode.getIndex();
        }
        netClone.setRoot(netClone.getNode(indexMap[netOrig.getRoot().getIndex()]));
        for (Node nodeOrig : netOrig.getNodes()) {
            if (nodeOrig == null) {
                continue;
            }
            Node nodeClone = netClone.getNode(indexMap[nodeOrig.getIndex()]);
            for (ArrowDirection direction : ArrowDirection.values()) {
                Node targetOrig = nodeOrig.getChild(direction, Purview.DIRECT);
                nodeClone.setChild(direction, netClone.getNode(indexMap[targetOrig.getIndex()]), Purview.DIRECT);
            }
        }
        return netClone;
    }

}
