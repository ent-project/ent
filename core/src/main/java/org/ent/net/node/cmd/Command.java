package org.ent.net.node.cmd;

import org.ent.net.Arrow;

public interface Command {

    ExecutionResult execute(Arrow parameters);

    int getValue();

	String getShortName();

    String getShortNameAscii();

}
