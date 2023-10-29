package org.ent.net.node.cmd.split;

import org.ent.permission.Permissions;
import org.ent.net.node.Node;

public abstract sealed class BiValueCondition implements BiCondition permits SameValueCondition, GreaterThanCondition {

    public boolean evaluate(Node node1, Node node2, Permissions permissions) {
        return test(node1.getValue(permissions), node2.getValue(permissions));
    }

    protected abstract boolean test(int value1, int value2);
}
