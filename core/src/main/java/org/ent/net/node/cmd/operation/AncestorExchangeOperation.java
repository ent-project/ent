package org.ent.net.node.cmd.operation;

import org.ent.net.Arrow;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;
import org.ent.permission.Permissions;
import org.ent.permission.WriteFacet;

public class AncestorExchangeOperation implements BiOperation {

	@Override
	public int getCode() {
		return Operations.CODE_ANCESTOR_EXCHANGE_OPERATION;
	}

	@Override
	public ExecutionResult apply(Arrow arrowToNode1, Arrow arrowToNode2, Permissions permissions) {
		Node node1 = arrowToNode1.getTarget(permissions);
		if (permissions.noWrite(node1, WriteFacet.ARROW)) return ExecutionResult.ERROR;

		Node node2 = arrowToNode2.getTarget(permissions);
		if (node1.getNet() != node2.getNet()) return ExecutionResult.ERROR;

		node1.getNet().ancestorExchange(node1, node2, permissions);
		return ExecutionResult.NORMAL;
	}

	@Override
	public String getShortName() {
		return "x";
	}
}
