package org.ent.net.node.cmd.operation;

import org.ent.net.node.cmd.split.Split;
import org.ent.net.node.cmd.split.SplitResult;
import org.ent.net.node.cmd.split.Splits;
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

        int value = node.getValue(permissions);
        if ((value & Command.COMMAND_PATTERN_AREA) == Command.COMMAND_PATTERN) {
            Command command = Commands.getByValue(value);
            if (command == null) {
                return ExecutionResult.ERROR;
            }
            if (command.isEval()) {
                return ExecutionResult.ERROR;
            }
            net.event(permissions).beforeEvalExecution(node, true);

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
        } else if ((value & Split.SPLIT_PATTERN_AREA) == Split.SPLIT_PATTERN) {
            Split split = Splits.getByValue(value);
            if (split == null) {
                return ExecutionResult.ERROR;
            }
            Permissions permissionsForNewActor = net.getPermissions();
            SplitResult splitResult = split.evaluate(node, permissionsForNewActor);

            // advance pointer
            Node branchingPoint = node.getRightChild(permissionsForNewActor);
            Node newTarget = branchingPoint.getChild(splitResult.getDirection(), permissionsForNewActor);
            if (newTarget.getNet() != net) {
                log.trace("cannot advance root as it would leave the net");
                return ExecutionResult.ERROR;
            } else {
                handle.setTarget(newTarget, permissions);
                net.setRoot(newTarget);
            }
            return toExecutionResult(splitResult);
        }
        return ExecutionResult.ERROR;
    }

    private ExecutionResult toExecutionResult(SplitResult splitResult) {
        return switch (splitResult) {
            case NORMAL_LEFT, NORMAL_RIGHT -> ExecutionResult.NORMAL;
            case ERROR -> ExecutionResult.ERROR;
        };
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
