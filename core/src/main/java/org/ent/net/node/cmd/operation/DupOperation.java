package org.ent.net.node.cmd.operation;

import org.ent.Ent;
import org.ent.net.Arrow;
import org.ent.net.AccessToken;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public class DupOperation implements BiOperation {

	@Override
	public int getCode() {
		return Operations.CODE_DUP_OPERATION;
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
		Node copy = target.getNet().newNode(target.getValue(Purview.COMMAND),
				target.getLeftChild(Purview.COMMAND),
				target.getRightChild(Purview.COMMAND));
		setter.setTarget(copy, Purview.COMMAND);
		ent.event().transverValue(target, copy);
		return ExecutionResult.NORMAL;
	}

	@Override
	public String getShortName() {
		return "dup";
	}
}
