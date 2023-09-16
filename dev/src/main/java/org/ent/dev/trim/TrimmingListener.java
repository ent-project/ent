package org.ent.dev.trim;

import org.ent.listener.NopNetEventListener;
import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.node.Node;

public class TrimmingListener extends NopNetEventListener {

    private final boolean[] isArrowOverridden;
    private final boolean[] isArrowRequired;

    public TrimmingListener(int size) {
        isArrowRequired = new boolean[size * 2];
        isArrowOverridden = new boolean[size * 2];
    }

    @Override
    public void calledGetChild(Node n, ArrowDirection arrowDirection) {
        Arrow arrow = n.getArrow(arrowDirection);
        int index = arrow.getIndex();
        if (index >= isArrowOverridden.length) {
            return;
        }
        if (!isArrowOverridden[index]) {
            isArrowRequired[index] = true;
        }
    }

    @Override
    public void calledSetChild(Node from, ArrowDirection arrowDirection, Node to) {
        Arrow arrow = from.getArrow(arrowDirection);
        int index = arrow.getIndex();
        if (index >= isArrowOverridden.length) {
            return;
        }
        isArrowOverridden[index] = true;
    }

    public boolean isDead(Arrow arrow) {
        return !isArrowRequired[arrow.getIndex()];
    }
}
