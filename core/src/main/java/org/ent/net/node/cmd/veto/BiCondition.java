package org.ent.net.node.cmd.veto;

import org.ent.net.Arrow;

public sealed interface BiCondition permits IdenticalCondition, BiValueCondition {

    int getCode();

    boolean evaluate(Arrow handle1, Arrow handle2);

    String getShortName();

    default String getInvertedShortName() {
        return "!" + getShortName();
    }
}
