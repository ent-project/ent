package org.ent.net.node.cmd.operation;

import org.ent.Ent;
import org.ent.net.Arrow;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.CommandFactory;
import org.ent.net.node.cmd.ExecutionResult;

/**
 * @deprecated not used right now, unclear if it is an operation that adds any new functionality
 */
@Deprecated(forRemoval = true)
public class EvalOperation implements BiOperation {

	private final int evalLevel;

	private final String shortName;

	@Override
	public int getCode() {
		throw new UnsupportedOperationException();
	}

	public EvalOperation(int evalLevel) {
		this.evalLevel = evalLevel;
		this.shortName = "eval" + evalLevel;
	}

	@Override
	public ExecutionResult apply(Arrow arrowToNode1, Arrow arrowToNode2, Ent ent) {
		Node node1 = arrowToNode1.getTarget(Purview.COMMAND);
		Command command = CommandFactory.getByValue(node1.getValue());
		if (command == null) {
			return ExecutionResult.ERROR;
		}
//		if (command.getEvalLevel() >= evalLevel) {
//			return ExecutionResult.ERROR;
//		}

		command.execute(arrowToNode2, null);
		return ExecutionResult.NORMAL;
	}

	public int getEvalLevel() {
		return evalLevel;
	}

	@Override
	public String getShortName() {
		return shortName;
	}

}
