package org.ent.listener;

import org.ent.net.Arrow;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.operation.TriValueOperation;
import org.ent.run.StepResult;

public class NopEntEventListener implements EntEventListener {

    @Override
    public void domainBreachAttemptInSet(Arrow setter, Node target) {
        // do nothing
    }

    @Override
    public void beforeCommandExecution(Node executionPointer, Command command) {
        // do nothing
    }

    @Override
    public void afterCommandExecution(StepResult stepResult) {
        // do nothing
    }

    @Override
    public void triValueOperation(Node node1, Node node2, Node node3, TriValueOperation operation) {
        // do nothing
    }

    @Override
    public void executionPointerTryingToLeaveNet(Node executionPointer, Node newExecutionPointer) {
        // do nothing
    }
}
