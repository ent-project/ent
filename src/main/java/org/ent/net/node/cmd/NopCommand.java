package org.ent.net.node.cmd;

import org.ent.net.NetController;
import org.ent.net.node.Node;

public class NopCommand implements Command {

	@Override
	public ExecuteResult execute(NetController controller, Node parameters) {
		return ExecuteResult.NORMAL;
	}

	@Override
	public String getShortName() {
		return "nop";
	}

}
