package org.ent.net.node.cmd.veto;

import org.ent.Ent;
import org.ent.net.Purview;
import org.ent.net.node.Node;

public abstract sealed class BiValueCondition implements BiCondition permits SameValueCondition, GreaterThanCondition {

    public boolean evaluate(Node node1, Node node2, Ent ent) {
        return test(node1.getValue(Purview.COMMAND), node2.getValue(Purview.COMMAND));
    }

    protected abstract boolean test(int value1, int value2);
}
