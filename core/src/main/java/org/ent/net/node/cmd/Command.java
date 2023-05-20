package org.ent.net.node.cmd;

import org.ent.Ent;
import org.ent.net.Arrow;

public interface Command {

    ExecutionResult execute(Arrow parameters, Ent ent);

    int getValue();

    String getShortNameAscii();

    default String getName() {
        return getShortNameAscii();
    }
}
