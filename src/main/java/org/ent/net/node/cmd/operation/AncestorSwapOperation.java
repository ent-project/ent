package org.ent.net.node.cmd.operation;

import org.ent.net.NetController;
import org.ent.net.node.Node;

public class AncestorSwapOperation implements BiOperation<Node, Node> {

	@Override
	public void apply(NetController controller, Node node1, Node node2) {
		controller.ancestorSwap(node1, node2);
	}

	@Override
	public String getShortName() {
		return "⤩";
	}

}
