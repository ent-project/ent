package org.ent.net.node.cmd.operation;

import org.ent.net.Arrow;
import org.ent.net.NetController;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecuteResult;

public class SetOperation implements BiOperation<Arrow, Node> {

	@Override
	public ExecuteResult apply(NetController controller, Arrow setter, Node target) {
		setter.setTarget(controller, target);
		return ExecuteResult.NORMAL;
	}

	@Override
	public String getShortName() {
		return "◄";
	}

}
