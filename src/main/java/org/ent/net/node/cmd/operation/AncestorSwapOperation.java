package org.ent.net.node.cmd.operation;

import org.ent.net.NetController;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public class AncestorSwapOperation implements BiOperation<Node, Node> {

	@Override
	public ExecutionResult apply(NetController controller, Node node1, Node node2) {
		controller.ancestorSwap(node1, node2);
		return ExecutionResult.NORMAL;
	}

	@Override
	public String getShortName() {
		return "⤩";
	}

}
