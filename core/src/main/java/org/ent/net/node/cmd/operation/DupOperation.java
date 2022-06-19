package org.ent.net.node.cmd.operation;

import org.ent.net.Arrow;
import org.ent.net.Purview;
import org.ent.net.Net;
import org.ent.net.node.BNode;
import org.ent.net.node.CNode;
import org.ent.net.node.Node;
import org.ent.net.node.UNode;
import org.ent.net.node.cmd.ExecutionResult;

public class DupOperation implements BiOperation<Arrow, Node> {

	@Override
	public ExecutionResult apply(Arrow setter, Node target) {
		Node copy;
		Net net = target.getNet();
		if (target instanceof CNode targetCNode) {
			copy = net.newCNode(targetCNode.getCommand());
		} else if (target instanceof UNode targetUNode) {
			copy = net.newUNode(targetUNode.getChild(Purview.COMMAND));
		} else if (target instanceof BNode targetBNode) {
			copy = net.newBNode(
					targetBNode.getLeftChild(Purview.COMMAND),
					targetBNode.getRightChild(Purview.COMMAND));
		} else {
			throw new AssertionError("Unexpected Node type: " + target.getClass());
		}
		setter.setTarget(copy, Purview.COMMAND);
		return ExecutionResult.NORMAL;
	}

	@Override
	public String getShortName() {
		return "⏪";
	}

	@Override
	public String getShortNameAscii() {
		return "dup";
	}
}
