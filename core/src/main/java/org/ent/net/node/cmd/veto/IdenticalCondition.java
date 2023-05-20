package org.ent.net.node.cmd.veto;

import org.ent.net.Arrow;
import org.ent.net.Purview;
import org.ent.net.node.Node;

public final class IdenticalCondition implements BiCondition {
    @Override
    public int getCode() {
        return Conditions.CODE_IDENTICAL_CONDITION;
    }

    @Override
    public boolean evaluate(Arrow handle1, Arrow handle2) {
        Node node1 = handle1.getTarget(Purview.COMMAND);
        Node node2 = handle2.getTarget(Purview.COMMAND);
        return node1 == node2;
    }

    @Override
    public String getShortName() {
        return "===";
    }

    @Override
    public String getInvertedShortName() {
        return "!==";
    }
}
