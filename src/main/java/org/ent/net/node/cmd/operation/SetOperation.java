package org.ent.net.node.cmd.operation;

import org.ent.net.Arrow;
import org.ent.net.NetController;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public class SetOperation implements BiOperation<Arrow, Node> {

	@Override
	public ExecutionResult apply(NetController controller, Arrow setter, Node target) {
		setter.setTarget(controller, target);
		return ExecutionResult.NORMAL;
	}

	@Override
	public String getShortName() {
		return "◄";
	}

	@Override
	public String getShortNameAscii() {
		return ":";
	}

}
