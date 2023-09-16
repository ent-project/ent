package org.ent.net.node.cmd.operation;

import org.ent.permission.Permissions;
import org.ent.net.Net;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvalOperation extends MonoNodeOperation {

	private final static Logger log = LoggerFactory.getLogger(EvalOperation.class);

	@Override
	public int getCode() {
		return Operations.CODE_EVAL_OPERATION;
	}

	@Override
	public ExecutionResult doApply(Node node, Permissions permissions) {
		Net net = node.getNet();
		Command command = Commands.getByValue(node.getValue(permissions));
		if (command == null) {
			log.trace("EvalOperation results in error: target value is no command: {}", node);
			return ExecutionResult.ERROR;
		}
		if (command.isEval()) {
			return ExecutionResult.ERROR;
		}
		net.event().beforeEvalExecution(node, false);

		return command.execute(node, permissions);
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
