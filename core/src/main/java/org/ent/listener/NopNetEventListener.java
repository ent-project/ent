package org.ent.listener;

import org.ent.net.ArrowDirection;
import org.ent.net.node.Node;

public class NopNetEventListener implements NetEventListener {

    @Override
    public void calledGetChild(Node n, ArrowDirection arrowDirection) {
        // do nothing
    }

    @Override
    public void calledSetChild(Node from, ArrowDirection arrowDirection, Node to) {
        // do nothing
    }

    @Override
    public void calledNewNode(Node n) {
        // do nothing
    }

    @Override
    public void getValue(Node node) {
        // do nothing
    }

    @Override
    public void setValue(Node node, int previousValue, int newValue) {
        // do nothing
    }

    @Override
    public void beforeEvalExecution(Node target, boolean flow) {
        // do nothing
    }

    @Override
    public void setRoot(Node previousRoot, Node newRoot) {
        // do nothing
    }
}
