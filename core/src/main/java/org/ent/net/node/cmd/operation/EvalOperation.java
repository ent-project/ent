package org.ent.net.node.cmd.operation;

import org.ent.Ent;
import org.ent.net.AccessToken;
import org.ent.net.Net;
import org.ent.net.Purview;
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
		Command command = Commands.getByValue(node.getValue(Purview.COMMAND));
		if (command == null) {
			return ExecutionResult.ERROR;
		}
		if (command.isEval()) {
			return ExecutionResult.ERROR;
		}
		net.event().beforeEvalExecution(node, false);

		AccessToken evalToken = net.getEvalToken();
		ExecutionResult result = command.execute(node, ent, evalToken);
		return result;
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
