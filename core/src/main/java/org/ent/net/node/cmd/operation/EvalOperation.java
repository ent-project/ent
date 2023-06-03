package org.ent.net.node.cmd.operation;

import org.ent.Ent;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.ExecutionResult;

public class EvalOperation extends MonoNodeOperation {

	@Override
	public int getCode() {
		return Operations.CODE_EVAL_OPERATION;
	}

	@Override
	public ExecutionResult doApply(Node node, Ent ent) {
		Command command = Commands.getByValue(node.getValue());
		if (command == null) {
			return ExecutionResult.ERROR;
		}
		if (command.isEval()) {
			return ExecutionResult.ERROR;
		}
		return command.execute(node, ent);
	}

	@Override
	public String getShortName() {
		return "eval";
	}

}
