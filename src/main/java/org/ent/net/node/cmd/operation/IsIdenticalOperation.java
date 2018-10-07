package org.ent.net.node.cmd.operation;

import org.ent.net.NetController;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public class IsIdenticalOperation implements BiOperation<Node, Node> {

	@Override
	public ExecutionResult apply(NetController controller, Node node1, Node node2) {
		if (node1 == node2) {
			return ExecutionResult.JUMP;
		} else {
			return ExecutionResult.NORMAL;
		}
	}

	@Override
	public String getShortName() {
		return "≡";
	}

}
