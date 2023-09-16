package org.ent.net.node.cmd.operation;

import org.ent.permission.Permissions;
import org.ent.permission.WriteFacet;
import org.ent.net.Arrow;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public abstract class BiValueOperation implements BiOperation {

	@Override
	public ExecutionResult apply(Arrow handle1, Arrow handle2, Permissions permissions) {
		Node node1 = handle1.getTarget(permissions);
		if (permissions.noWrite(node1, WriteFacet.VALUE)) return ExecutionResult.ERROR;

		Node node2 = handle2.getTarget(permissions);

		node1.setValue(compute(node2.getValue(permissions)), permissions);
		return ExecutionResult.NORMAL;
	}

	public abstract int compute(int a);
}
