package org.ent.net.node.cmd.operation;

import org.ent.permission.Permissions;
import org.ent.permission.WriteFacet;
import org.ent.net.Arrow;
import org.ent.net.Net;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

/**
 * Similar to ancestor exchange, but for the two target nodes, arrows pointing to self are not affected.
 */
public class AncestorExchangeNormalOperation implements BiOperation {

	@Override
	public int getCode() {
		return Operations.CODE_ANCESTOR_EXCHANGE_NORMAL_OPERATION;
	}

	@Override
	public ExecutionResult apply(Arrow arrowToNode1, Arrow arrowToNode2, Permissions permissions) {
		Node node1 = arrowToNode1.getTarget(permissions);
		if (permissions.noWrite(node1, WriteFacet.ARROW)) return ExecutionResult.ERROR;

		Node node2 = arrowToNode2.getTarget(permissions);
		if (node1.getNet() != node2.getNet()) return ExecutionResult.ERROR;

		boolean node1HasProperLeftChild = node1.hasProperLeftChild(permissions);
		boolean node1HasProperRightChild = node1.hasProperRightChild(permissions);
		boolean node2HasProperLeftChild = node2.hasProperLeftChild(permissions);
		boolean node2HasProperRightChild = node2.hasProperRightChild(permissions);
		Net.ancestorExchange(node1, node2);
		if (!node1HasProperLeftChild) {
			node1.setLeftChild(node1, permissions);
		}
		if (!node1HasProperRightChild) {
			node1.setRightChild(node1, permissions);
		}
		if (!node2HasProperLeftChild) {
			node2.setLeftChild(node2, permissions);
		}
		if (!node2HasProperRightChild) {
			node2.setRightChild(node2, permissions);
		}
		return ExecutionResult.NORMAL;
	}

	@Override
	public String getShortName() {
		return "xn";
	}
}
