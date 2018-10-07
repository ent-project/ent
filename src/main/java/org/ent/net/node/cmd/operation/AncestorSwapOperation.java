package org.ent.net.node.cmd.operation;

import org.ent.net.NetController;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecuteResult;

public class AncestorSwapOperation implements BiOperation<Node, Node> {

	@Override
	public ExecuteResult apply(NetController controller, Node node1, Node node2) {
		controller.ancestorSwap(node1, node2);
		return ExecuteResult.NORMAL;
	}

	@Override
	public String getShortName() {
		return "⤩";
	}

}
