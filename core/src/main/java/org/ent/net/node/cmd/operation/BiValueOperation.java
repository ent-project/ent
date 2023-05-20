package org.ent.net.node.cmd.operation;

import org.ent.Ent;
import org.ent.net.Arrow;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public abstract class BiValueOperation implements BiOperation {

	@Override
	public ExecutionResult apply(Arrow handle1, Arrow handle2, Ent ent) {
		Node node1 = handle1.getTarget(Purview.COMMAND);
		Node node2 = handle2.getTarget(Purview.COMMAND);
		node1.setValue(compute(node2.getValue()));
		return ExecutionResult.NORMAL;
	}

	public abstract int compute(int a);
}
