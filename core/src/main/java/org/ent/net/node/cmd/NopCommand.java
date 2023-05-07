package org.ent.net.node.cmd;

import org.ent.net.Arrow;

public class NopCommand implements Command {

	@Override
	public ExecutionResult execute(Arrow parameters) {
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
