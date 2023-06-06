package org.ent.net.node.cmd.operation;

import org.ent.Ent;
import org.ent.net.AccessToken;
import org.ent.net.Net;
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
	public ExecutionResult doApply(Node node, Ent ent, AccessToken accessToken) {
		Net net = node.getNet();
		if (!net.isPermittedToEval(node)) {
			return ExecutionResult.ERROR;
		}
		Command command = Commands.getByValue(node.getValue());
		if (command == null) {
			return ExecutionResult.ERROR;
		}
		if (command.isEval()) {
			return ExecutionResult.ERROR;
		}
		AccessToken evalToken = net.getEvalToken();
		return command.execute(node, ent, evalToken);
	}

	@Override
	public String getShortName() {
		return "eval";
	}

}
