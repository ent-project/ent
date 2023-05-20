package org.ent.net.node.cmd;

import org.ent.Ent;
import org.ent.net.Arrow;

public class NopCommand implements Command {

	@Override
	public ExecutionResult execute(Arrow parameters, Ent ent) {
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
