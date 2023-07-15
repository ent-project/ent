package org.ent.net.node.cmd.operation;

import org.ent.Ent;
import org.ent.net.Arrow;
import org.ent.net.AccessToken;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

/**
 * Duplicate node. The copy has the same children as the original. There is one exception:
 * If the original has arrows pointing to itself, then the copy will also have arrows pointing to itself.
 */
public class DupNormalOperation implements BiOperation {

	@Override
	public int getCode() {
		return Operations.CODE_DUP_NORMAL_OPERATION;
	}

	@Override
	public ExecutionResult apply(Arrow setter, Arrow arrowToTarget, Ent ent, AccessToken accessToken) {
		Node target = arrowToTarget.getTarget(Purview.COMMAND);
		if (!setter.permittedToSetTarget(target, accessToken)) {
			return ExecutionResult.ERROR;
		}
		if (!target.getNet().isPermittedToWrite(accessToken)) {
			return ExecutionResult.ERROR;
		}
		Node copy = target.getNet().newNode(target.getValue(Purview.COMMAND));
		if (target.hasProperLeftChild()) {
			copy.setLeftChild(target.getLeftChild(Purview.COMMAND), Purview.DIRECT);
		}
		if (target.hasProperRightChild()) {
			copy.setRightChild(target.getRightChild(Purview.COMMAND), Purview.DIRECT);
		}
		setter.setTarget(copy, Purview.COMMAND);
		return ExecutionResult.NORMAL;
	}

	@Override
	public String getShortName() {
		return "dupn";
	}
}
