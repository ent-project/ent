package org.ent.net.node.cmd.veto;

import org.ent.net.Arrow;
import org.ent.net.Purview;
import org.ent.net.node.Node;

public abstract sealed class BiValueCondition implements BiCondition permits SameValueCondition, GreaterThanCondition {

    public boolean evaluate(Arrow handle1, Arrow handle2) {
        Node node1 = handle1.getTarget(Purview.COMMAND);
        Node node2 = handle2.getTarget(Purview.COMMAND);
        return test(node1.getValue(), node2.getValue());
    }

    protected abstract boolean test(int value1, int value2);
}
