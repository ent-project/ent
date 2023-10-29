package org.ent.net.node.cmd.split;

import org.ent.permission.Permissions;
import org.ent.net.node.Node;

public final class IdenticalCondition implements BiCondition {
    @Override
    public int getCode() {
        return Conditions.CODE_IDENTICAL_CONDITION;
    }

    @Override
    public boolean evaluate(Node node1, Node node2, Permissions permissions) {
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
