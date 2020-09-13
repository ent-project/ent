package org.ent.run;

import org.ent.net.Net;
import org.ent.net.NetController;
import org.ent.net.node.BNode;
import org.ent.net.node.CNode;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.ExecutionResult;

public class NetRunner {

	private final Net net;

	private final NetController controller;

	public NetRunner(Net net, NetController controller) {
		this.net = net;
		this.controller = controller;
	}

	public Net getNet() {
		return net;
	}

	public NetController getController() {
		return controller;
	}

	public StepResult step() {
		Node root = net.getRoot();
		if (!(root instanceof BNode executionPointer)) {
			return StepResult.FATAL;
		}
		StepResult result = doStep(executionPointer);
		advanceExecutionPointer(executionPointer);
		return result;
	}

	private StepResult doStep(BNode executionPointer) {
		if (!(executionPointer.getLeftChild(controller) instanceof BNode commandBranch)) {
			return StepResult.INVALID_COMMAND_BRANCH;
		}
		if (!(commandBranch.getLeftChild(controller) instanceof CNode commandNode)) {
			return StepResult.INVALID_COMMAND_NODE;
		}
		Command command = commandNode.getCommand();
		Node parameters = commandBranch.getRightChild(controller);

		ExecutionResult executeResult = command.execute(controller, parameters);

		return convertToStepResult(executeResult);
	}

	private StepResult convertToStepResult(ExecutionResult executeResult) throws AssertionError {
		return switch (executeResult) {
			case NORMAL -> StepResult.SUCCESS;
			case ERROR -> StepResult.COMMAND_EXECUTION_FAILED;
		};
	}

	private void advanceExecutionPointer(BNode executionPointer) {
		Node newExecutionPointer = executionPointer.getRightChild(controller);
		net.setRoot(newExecutionPointer);
	}
}
