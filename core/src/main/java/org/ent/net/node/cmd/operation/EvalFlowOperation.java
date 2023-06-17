package org.ent.net.node.cmd.operation;

import org.ent.Ent;
import org.ent.net.AccessToken;
import org.ent.net.Arrow;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.ExecutionResult;

public class EvalFlowOperation implements MonoOperation {
    @Override
    public int getCode() {
        return Operations.CODE_EVAL_FLOW_OPERATION;
    }

    @Override
    public ExecutionResult apply(Arrow handle, Ent ent, AccessToken accessToken) {
        Node node = handle.getTarget(Purview.COMMAND);
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
        AccessToken evalToken = net.getEvalToken();
        ExecutionResult executionResult = command.execute(node, ent, evalToken);
        ent.event().evalFloatOperation(node);
        // advance pointer
        Node newTarget = node.getRightChild(Purview.COMMAND);
        handle.setTarget(newTarget, Purview.COMMAND, evalToken);

        return executionResult;
    }

    @Override
    public boolean isEval() {
        return true;
    }

    @Override
    public String getShortName() {
        return "eval_flow";
    }
}