package org.ent.net.node.cmd.operation;

import org.ent.permission.Permissions;
import org.ent.net.Arrow;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

/**
 * @deprecated probably no longer needed and currently not maintained
 */
@Deprecated(forRemoval = true)
public class IsIdenticalOperation implements BiOperation {

	@Override
	public int getCode() {
		return Operations.CODE_IS_IDENTICAL_OPERATION;
	}

	@Override
	public ExecutionResult apply(Arrow arrow1, Arrow arrow2, Permissions permissions) {
		Node node1 = arrow1.getTarget(permissions);
		Node node2 = arrow2.getTarget(permissions);
		if (node1.getNet() != node2.getNet()) {
			return ExecutionResult.ERROR;
		}
		if (evaluateCondition(node1, node2)) {
			arrow1.setTarget(arrow1.getOrigin(), permissions);
		} else {
			arrow1.setTarget(arrow2.getOrigin(), permissions);
		}
		return ExecutionResult.NORMAL;
	}

	private boolean evaluateCondition(Node node1, Node node2) {
		return node1 == node2;
	}

	@Override
	public String getShortName() {
		return "==";
	}

}
