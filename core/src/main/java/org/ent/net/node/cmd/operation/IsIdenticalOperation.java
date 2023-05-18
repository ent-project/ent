package org.ent.net.node.cmd.operation;

import org.ent.net.Arrow;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public class IsIdenticalOperation implements BiOperation {

	@Override
	public int getCode() {
		return Operations.CODE_IS_IDENTICAL_OPERATION;
	}

	@Override
	public ExecutionResult apply(Arrow arrow1, Arrow arrow2) {
		Node node1 = arrow1.getTarget(Purview.COMMAND);
		Node node2 = arrow2.getTarget(Purview.COMMAND);
		if (node1.getNet() != node2.getNet()) {
			return ExecutionResult.ERROR;
		}
		if (evaluateCondition(node1, node2)) {
			arrow1.setTarget(arrow1.getOrigin(), Purview.COMMAND);
		} else {
			arrow1.setTarget(arrow2.getOrigin(), Purview.COMMAND);
		}
		return ExecutionResult.NORMAL;
	}

	private boolean evaluateCondition(Node node1, Node node2) {
		return node1 == node2;
	}

	@Override
	public String getShortName() {
		return "≡";
	}

	@Override
	public String getShortNameAscii() {
		return "==";
	}

}
