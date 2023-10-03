package org.ent.listener;

import org.ent.net.Arrow;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.operation.TriValueOperation;
import org.ent.run.StepResult;

import java.util.ArrayList;
import java.util.List;

public class MultiEntEventListener implements EntEventListener {

    List<EntEventListener> eventListeners = new ArrayList<>();

    public void addEntEventListener(EntEventListener entEventListener) {
        eventListeners.add(entEventListener);
    }

    @Override
    public void domainBreachAttemptInSet(Arrow setter, Node target) {
        for (EntEventListener eventListener : eventListeners) {
            eventListener.domainBreachAttemptInSet(setter, target);
        }
    }

    @Override
    public void beforeCommandExecution(Node executionPointer, Command command) {
        for (EntEventListener eventListener : eventListeners) {
            eventListener.beforeCommandExecution(executionPointer, command);
        }
    }

    @Override
    public void afterCommandExecution(StepResult stepResult) {
        for (EntEventListener eventListener : eventListeners) {
            eventListener.afterCommandExecution(stepResult);
        }
    }

    @Override
    public void triValueOperation(Node node1, Node node2, Node node3, TriValueOperation operation) {
        for (EntEventListener eventListener : eventListeners) {
            eventListener.triValueOperation(node1, node2, node3, operation);
        }
    }

    @Override
    public void executionPointerTryingToLeaveNet(Node executionPointer, Node newExecutionPointer) {
        for (EntEventListener eventListener : eventListeners) {
            eventListener.executionPointerTryingToLeaveNet(executionPointer, newExecutionPointer);
        }
    }
}
