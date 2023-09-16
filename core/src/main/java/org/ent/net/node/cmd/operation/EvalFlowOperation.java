package org.ent.net.node.cmd.operation;

import org.ent.permission.Permissions;
import org.ent.net.Arrow;
import org.ent.net.Net;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvalFlowOperation implements MonoOperation {

    private final static Logger log = LoggerFactory.getLogger(EvalFlowOperation.class);

    @Override
    public int getCode() {
        return Operations.CODE_EVAL_FLOW_OPERATION;
    }

    @Override
    public ExecutionResult apply(Arrow handle, Permissions permissions) {
        if (permissions.noExecute(handle)) return ExecutionResult.ERROR;
        Node node = handle.getTarget(permissions);
        Net net = node.getNet();
        if (node != net.getRoot()) return ExecutionResult.ERROR;

        Command command = Commands.getByValue(node.getValue(permissions));
        if (command == null) {
            return ExecutionResult.ERROR;
        }
        if (command.isEval()) {
            return ExecutionResult.ERROR;
        }
        net.event().beforeEvalExecution(node, true);

        Permissions permissionsForNewActor = net.getPermissions();
        ExecutionResult executionResult = command.execute(node, permissionsForNewActor);

        // advance pointer
        Node newTarget = node.getRightChild(permissions);
        if (newTarget.getNet() != net) {
            log.trace("cannot advance root as it would leave the net");
            return ExecutionResult.ERROR;
        } else {
            handle.setTarget(newTarget, permissions);
            net.setRoot(newTarget);
        }
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
