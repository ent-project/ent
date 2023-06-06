package org.ent.net.node.cmd.operation;

import org.ent.Ent;
import org.ent.net.Arrow;
import org.ent.net.AccessToken;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public class AncestorExchangeOperation implements BiOperation {

	@Override
	public int getCode() {
		return Operations.CODE_ANCESTOR_EXCHANGE_OPERATION;
	}

	@Override
	public ExecutionResult apply(Arrow arrowToNode1, Arrow arrowToNode2, Ent ent, AccessToken accessToken) {
		Node node1 = arrowToNode1.getTarget(Purview.COMMAND);
		if (!node1.getNet().isPermittedToWrite(accessToken)) {
			return ExecutionResult.ERROR;
		}
		Node node2 = arrowToNode2.getTarget(Purview.COMMAND);
		if (node1.getNet() != node2.getNet()) {
			return ExecutionResult.ERROR;
		}
		Net.ancestorExchange(node1, node2);
		return ExecutionResult.NORMAL;
	}

	@Override
	public String getShortName() {
		return "x";
	}
}
