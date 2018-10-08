package org.ent.net.node.cmd.operation;

import org.ent.net.NetController;
import org.ent.net.node.CNode;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.ExecutionResult;

public class EvalOperation implements BiOperation<Node, Node> {

	private final int evalLevel;

	public EvalOperation(int evalLevel) {
		this.evalLevel = evalLevel;
	}

	@Override
	public ExecutionResult apply(NetController controller, Node node1, Node node2) {
		if (!(node1 instanceof CNode)) {
			return ExecutionResult.ERROR;
		}
		CNode cNode = (CNode) node1;
		Command command = cNode.getCommand();
		if (command.getEvalLevel() >= evalLevel) {
			return ExecutionResult.ERROR;
		}

		command.execute(controller, node2);
		return ExecutionResult.NORMAL;
	}

	@Override
	public int getEvalLevel() {
		return evalLevel;
	}

	@Override
	public String getShortName() {
		return "🞜";
	}

}
