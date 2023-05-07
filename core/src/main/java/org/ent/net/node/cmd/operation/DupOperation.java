package org.ent.net.node.cmd.operation;

import org.ent.net.Arrow;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public class DupOperation implements BiOperation {

	@Override
	public int getCode() {
		return Operations.CODE_DUP_OPERATION;
	}

	@Override
	public ExecutionResult apply(Arrow setter, Arrow arrowToTarget) {
		Node target = arrowToTarget.getTarget(Purview.COMMAND);
		Node copy = target.getNet().newNode(target.getValue(),
				target.getLeftChild(Purview.COMMAND),
				target.getRightChild(Purview.COMMAND));
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
