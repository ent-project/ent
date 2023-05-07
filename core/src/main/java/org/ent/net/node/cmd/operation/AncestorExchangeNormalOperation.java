package org.ent.net.node.cmd.operation;

import org.ent.net.Arrow;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

/**
 * Similar to ancestor exchange, but for the two target nodes, arrows pointing to self are not affected.
 */
public class AncestorExchangeNormalOperation implements BiOperation {

	@Override
	public int getCode() {
		return Operations.CODE_ANCESTOR_SWAP_OPERATION;
	}

	@Override
	public ExecutionResult apply(Arrow arrowToNode1, Arrow arrowToNode2) {
		Node node1 = arrowToNode1.getTarget(Purview.COMMAND);
		boolean node1HasProperLeftChild = node1.hasProperLeftChild();
		boolean node1HasProperRightChild = node1.hasProperRightChild();
		Node node2 = arrowToNode2.getTarget(Purview.COMMAND);
		boolean node2HasProperLeftChild = node2.hasProperLeftChild();
		boolean node2HasProperRightChild = node2.hasProperRightChild();
		Net.ancestorExchange(node1, node2);
		if (!node1HasProperLeftChild) {
			node1.setLeftChild(node1, Purview.COMMAND);
		}
		if (!node1HasProperRightChild) {
			node1.setRightChild(node1, Purview.COMMAND);
		}
		if (!node2HasProperLeftChild) {
			node2.setLeftChild(node2, Purview.COMMAND);
		}
		if (!node2HasProperRightChild) {
			node2.setRightChild(node2, Purview.COMMAND);
		}
		return ExecutionResult.NORMAL;
	}

	@Override
	public String getShortName() {
		return "⤩n";
	}

	@Override
	public String getShortNameAscii() {
		return "xn";
	}
}
