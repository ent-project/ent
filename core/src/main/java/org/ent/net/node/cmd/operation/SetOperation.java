package org.ent.net.node.cmd.operation;

import org.ent.Ent;
import org.ent.net.Arrow;
import org.ent.net.AccessToken;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public class SetOperation implements BiOperation {

	@Override
	public int getCode() {
		return Operations.CODE_SET_OPERATION;
	}

	@Override
	public ExecutionResult apply(Arrow setter, Arrow arrowToTarget, Ent ent, AccessToken accessToken) {
		Node target = arrowToTarget.getTarget(Purview.COMMAND);
		if (!setter.permittedToSetTarget(target, accessToken)) {
			ent.event().domainBreachAttemptInSet(setter, target);
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
