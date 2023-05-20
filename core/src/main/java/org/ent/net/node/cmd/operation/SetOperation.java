package org.ent.net.node.cmd.operation;

import org.ent.net.Arrow;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public class SetOperation implements BiOperation {

	@Override
	public int getCode() {
		return Operations.CODE_SET_OPERATION;
	}

	@Override
	public ExecutionResult apply(Arrow setter, Arrow arrowToTarget) {
		Node target = arrowToTarget.getTarget(Purview.COMMAND);
		if (target.getNet() != setter.getOrigin().getNet()) {
			return ExecutionResult.ERROR;
		}
		setter.setTarget(target, Purview.COMMAND);
		return ExecutionResult.NORMAL;
	}

	@Override
	public String getShortName() {
		return "=";
	}

}
