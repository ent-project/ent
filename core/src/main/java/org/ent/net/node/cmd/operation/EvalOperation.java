package org.ent.net.node.cmd.operation;

import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.ExecutionResult;
import org.ent.permission.Permissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvalOperation extends MonoNodeOperation {

	private final static Logger log = LoggerFactory.getLogger(EvalOperation.class);

	@Override
	public int getCode() {
		return Operations.CODE_EVAL_OPERATION;
	}

	@Override
	public ExecutionResult doApply(Node commandNode, Permissions permissions) {
		Command command = Commands.getByValue(commandNode.getValue(permissions));
		if (command == null) {
			log.trace("EvalOperation results in error: target value is no command: {}", commandNode);
			return ExecutionResult.ERROR;
		}
		if (command.isEval()) {
			return ExecutionResult.ERROR;
		}
		commandNode.getNet().event(permissions).beforeEvalExecution(commandNode, false);

		return command.execute(commandNode, permissions);
	}

	@Override
	public boolean isEval() {
		return true;
	}

	@Override
	public String getShortName() {
		return "eval";
	}

}
