package org.ent.net.node.cmd.split;

import org.ent.permission.Permissions;
import org.ent.net.node.Node;

public sealed interface BiCondition permits IdenticalCondition, BiValueCondition {

    int getCode();

    boolean evaluate(Node node1, Node node2, Permissions permissions);

    String getShortName();

    default String getInvertedShortName() {
        return "!" + getShortName();
    }
}
