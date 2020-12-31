package org.ent.net.node.cmd.operation;

import org.ent.net.Net;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public class AncestorSwapOperation implements BiOperation<Node, Node> {

	@Override
	public ExecutionResult apply(Node node1, Node node2) {
		Net.ancestorSwap(node1, node2);
		return ExecutionResult.NORMAL;
	}

	@Override
	public String getShortName() {
		return "⤩";
	}

	@Override
	public String getShortNameAscii() {
		return "ix";
	}
}
