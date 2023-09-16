package org.ent.net.node.cmd.operation;

import org.ent.permission.Permissions;
import org.ent.permission.WriteFacet;
import org.ent.net.Arrow;
import org.ent.net.Net;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public class DupOperation implements BiOperation {

	@Override
	public int getCode() {
		return Operations.CODE_DUP_OPERATION;
	}

	@Override
	public ExecutionResult apply(Arrow setter, Arrow arrowToTarget, Permissions permissions) {
		Net originNet = setter.getOrigin().getNet();
		if (permissions.noWrite(originNet, WriteFacet.NEW_NODE)) return ExecutionResult.ERROR;
		if (permissions.noWrite(originNet, WriteFacet.ARROW)) return ExecutionResult.ERROR;

		Node target = arrowToTarget.getTarget(permissions);

		Permissions originPermissions = originNet.getPermissions();
		if (originPermissions.noPointTo(target.getLeftChild(permissions))) return ExecutionResult.ERROR;
		if (originPermissions.noPointTo(target.getRightChild(permissions))) return ExecutionResult.ERROR;

		Node copy = originNet.newNode(target.getValue(permissions),
				target.getLeftChild(permissions),
				target.getRightChild(permissions),
				permissions);
		setter.setTarget(copy, permissions);
		return ExecutionResult.NORMAL;
	}

	@Override
	public String getShortName() {
		return "dup";
	}
}
