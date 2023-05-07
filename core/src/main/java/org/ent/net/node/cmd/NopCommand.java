package org.ent.net.node.cmd;

import org.ent.net.node.Node;

public class NopCommand implements Command {

	@Override
	public ExecutionResult execute(Node parameters) {
		return ExecutionResult.NORMAL;
	}

	@Override
	public int getValue() {
		return 0;
	}

	@Override
	public String getShortName() {
		return "∅";
	}

	@Override
	public String getShortNameAscii() {
		return "o";
	}
}
