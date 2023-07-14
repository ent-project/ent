package org.ent;

import org.ent.net.ArrowDirection;
import org.ent.net.Purview;
import org.ent.net.node.Node;

public class NopNetEventListener implements NetEventListener {
    @Override
    public void calledGetChild(Node n, ArrowDirection arrowDirection, Purview purview) {
        // do nothing
    }

    @Override
    public void calledSetChild(Node from, ArrowDirection arrowDirection, Node to, Purview purview) {
        // do nothing
    }

    @Override
    public void calledNewNode(Node n) {
        // do nothing
    }

    @Override
    public void getValue(Node node, Purview purview) {
        // do nothing
    }

    @Override
    public void setValue(Node node, int previousValue, int newValue) {
        // do nothing
    }

    @Override
    public void evaluatedIsIdenticalCondition(Node node1, Node node2) {
        // do nothing
    }

    @Override
    public void beforeEvalExecution(Node target, boolean flow) {
        // do nothing
    }
}
