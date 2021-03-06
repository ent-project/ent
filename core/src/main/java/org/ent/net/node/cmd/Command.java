package org.ent.net.node.cmd;

import org.ent.net.node.Node;

public interface Command {

    ExecutionResult execute(Node parameters);

    int getEvalLevel();

	String getShortName();

    String getShortNameAscii();

}
