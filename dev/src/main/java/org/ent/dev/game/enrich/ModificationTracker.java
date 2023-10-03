package org.ent.dev.game.enrich;

import org.ent.listener.NopNetEventListener;
import org.ent.net.ArrowDirection;
import org.ent.net.node.Node;
import org.ent.permission.Permissions;

public class ModificationTracker extends NopNetEventListener {

    private boolean dirty;

    public boolean wasModified() {
        return dirty;
    }

    public void reset() {
        dirty = false;
    }

    @Override
    public void setValue(Node node, int previousValue, int newValue) {
        if (previousValue != newValue) {
            netModification();
        }
    }

    @Override
    public void calledSetChild(Node from, ArrowDirection arrowDirection, Node to) {
        Node currentChild = from.getChild(arrowDirection, Permissions.DIRECT);
        if (currentChild != to) {
            netModification();
        }
    }

    @Override
    public void calledNewNode(Node n) {
        netModification();
    }

    @Override
    public void ancestorExchange(Node node1, Node node2) {
        if (node1 != node2) {
            netModification();
        }
    }

    @Override
    public void setRoot(Node previousRoot, Node newRoot) {
        throw new IllegalStateException("Root change not expected for this domain");
    }

    private void netModification() {
        dirty = true;
    }

}
