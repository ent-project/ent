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

    public NetCopyPack(Net net) {
        this.netOrig = net;
        this.indexMap = new int[netOrig.getNodes().size()];
    }

    public Net createCopy() {
        Net netClone = new Net();
        doCopy(netClone);
        netClone.setRoot(netClone.getNode(indexMap[netOrig.getRoot().getIndex()]));
        return netClone;
    }

    public void copyIntoExistingNet(Net receivingNet) {
        doCopy(receivingNet);
    }

    public int getCopiedNodeIndex(Node nodeFromOriginal) {
        return indexMap[nodeFromOriginal.getIndex()];
    }

    private void doCopy(Net receivingNet) {
        for (Node nodeOrig : netOrig.getNodes()) {
            if (nodeOrig == null) {
                continue;
            }
            Node newNode = receivingNet.newNode(nodeOrig.getValue(Purview.DIRECT));
            indexMap[nodeOrig.getIndex()] = newNode.getIndex();
        }
        for (Node nodeOrig : netOrig.getNodes()) {
            if (nodeOrig == null) {
                continue;
            }
            Node nodeClone = receivingNet.getNode(indexMap[nodeOrig.getIndex()]);
            for (ArrowDirection direction : ArrowDirection.values()) {
                Node targetOrig = nodeOrig.getChild(direction, Purview.DIRECT);
                nodeClone.setChild(direction, receivingNet.getNode(indexMap[targetOrig.getIndex()]), Purview.DIRECT);
            }
        }
    }
}
