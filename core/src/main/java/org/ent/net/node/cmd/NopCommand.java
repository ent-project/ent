package org.ent.net.node.cmd;

import org.ent.Ent;
import org.ent.net.node.Node;

public class NopCommand implements Command {

	@Override
	public ExecutionResult execute(Node base, Ent ent) {
		return ExecutionResult.NORMAL;
	}

	@Override
	public int getValue() {
		return 0;
	}

	public String getShortName() {
		return "o";
	}

	@Override
	public String toString() {
		return "<" + getShortName() + ">";
	}
}
