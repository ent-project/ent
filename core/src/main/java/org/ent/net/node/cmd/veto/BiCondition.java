package org.ent.net.node.cmd.veto;

import org.ent.net.node.Node;

public sealed interface BiCondition permits IdenticalCondition, BiValueCondition {

    int getCode();

    boolean evaluate(Node node1, Node node2);

    String getShortName();

    default String getInvertedShortName() {
        return "!" + getShortName();
    }
}
