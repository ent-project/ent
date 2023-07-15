package org.ent;

import org.ent.net.ArrowDirection;
import org.ent.net.Purview;
import org.ent.net.node.Node;

import java.util.ArrayList;
import java.util.List;

public class MultiNetEventListener implements NetEventListener {

    List<NetEventListener> eventListeners = new ArrayList<>();

    public void addNetEventListener(NetEventListener netEventListener) {
        eventListeners.add(netEventListener);
    }

    @Override
    public void calledGetChild(Node n, ArrowDirection arrowDirection, Purview purview) {
        for (NetEventListener eventListener : eventListeners) {
            eventListener.calledGetChild(n, arrowDirection, purview);
        }
    }

    @Override
    public void calledSetChild(Node from, ArrowDirection arrowDirection, Node to, Purview purview) {
        for (NetEventListener eventListener : eventListeners) {
            eventListener.calledSetChild(from, arrowDirection, to, purview);
        }
    }

    @Override
    public void calledNewNode(Node n) {
        for (NetEventListener eventListener : eventListeners) {
            eventListener.calledNewNode(n);
        }
    }

    @Override
    public void getValue(Node node, Purview purview) {
        for (NetEventListener eventListener : eventListeners) {
            eventListener.getValue(node, purview);
        }
    }

    @Override
    public void setValue(Node node, int previousValue, int newValue) {
        for (NetEventListener eventListener : eventListeners) {
            eventListener.setValue(node, previousValue, newValue);
        }

    }

    @Override
    public void evaluatedIsIdenticalCondition(Node node1, Node node2) {
        for (NetEventListener eventListener : eventListeners) {
            eventListener.evaluatedIsIdenticalCondition(node1, node2);
        }
    }

    @Override
    public void beforeEvalExecution(Node target, boolean flow) {
        for (NetEventListener eventListener : eventListeners) {
            eventListener.beforeEvalExecution(target, flow);
        }
    }

    @Override
    public void setRoot(Node previousRoot, Node newRoot) {
        for (NetEventListener eventListener : eventListeners) {
            eventListener.setRoot(previousRoot, newRoot);
        }
    }
}
