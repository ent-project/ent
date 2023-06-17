package org.ent.net.node.cmd.veto;

import org.ent.Ent;
import org.ent.net.node.Node;

public final class IdenticalCondition implements BiCondition {
    @Override
    public int getCode() {
        return Conditions.CODE_IDENTICAL_CONDITION;
    }

    @Override
    public boolean evaluate(Node node1, Node node2, Ent ent) {
        if (node1.getNet() == node2.getNet()) {
            ent.getNet().event().evaluatedIsIdenticalCondition(node1, node2);
        }
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
