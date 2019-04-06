package org.ent.net.node.cmd;

import org.ent.net.NetController;
import org.ent.net.node.Node;

public class NopCommand implements Command {

	@Override
	public ExecutionResult execute(NetController controller, Node parameters) {
		return ExecutionResult.NORMAL;
	}

	@Override
	public int getEvalLevel() {
		return 0;
	}

	@Override
	public String getShortName() {
		return "∅";
	}

	@Override
	public String getShortNameAscii() {
		return "nop";
	}
}
