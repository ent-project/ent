package org.ent.listener;

import org.ent.net.Arrow;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.operation.TriValueOperation;
import org.ent.run.StepResult;

public interface EntEventListener {

	void domainBreachAttemptInSet(Arrow setter, Node target);

    void beforeCommandExecution(Node executionPointer, Command command);

	void afterCommandExecution(StepResult stepResult);

	void triValueOperation(Node node1, Node node2, Node node3, TriValueOperation operation);

    void executionPointerTryingToLeaveNet(Node executionPointer, Node newExecutionPointer);
}
