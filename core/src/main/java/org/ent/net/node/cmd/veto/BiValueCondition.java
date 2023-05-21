package org.ent.net.node.cmd.veto;

import org.ent.net.node.Node;

public abstract sealed class BiValueCondition implements BiCondition permits SameValueCondition, GreaterThanCondition {

    public boolean evaluate(Node node1, Node node2) {
        return test(node1.getValue(), node2.getValue());
    }

    protected abstract boolean test(int value1, int value2);
}
