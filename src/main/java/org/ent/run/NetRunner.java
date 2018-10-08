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

	public StepResult step() {
		Node root = net.getRoot();
		if (!(root instanceof BNode)) {
			return StepResult.FATAL;
		}
		BNode executionPointer = (BNode) root;
		StepResult result = doStep(executionPointer);
		advanceExecutionPointer(executionPointer);
		return result;
	}

	private StepResult doStep(BNode executionPointer) {
		Node commandBranchNode = executionPointer.getLeftChild(controller);
		if (!(commandBranchNode instanceof BNode)) {
			return StepResult.INVALID_COMMAND_BRANCH;
		}
		BNode commandBranch = (BNode) commandBranchNode;
		Node commandNode = commandBranch.getLeftChild(controller);
		if (!(commandNode instanceof CNode)) {
			return StepResult.INVALID_COMMAND_NODE;
		}
		Command command = ((CNode) commandNode).getCommand();
		Node parameters = commandBranch.getRightChild(controller);

		ExecutionResult executeResult = command.execute(controller, parameters);

		return convertToStepResult(executeResult);
	}

	private StepResult convertToStepResult(ExecutionResult executeResult) throws AssertionError {
		switch (executeResult) {
		case NORMAL: return StepResult.SUCCESS;
		case ERROR: return StepResult.COMMAND_EXECUTION_FAILED;
		default: throw new AssertionError("Unexpected execution result: " + executeResult);
		}
	}

	private void advanceExecutionPointer(BNode executionPointer) {
		Node newExecutionPointer = executionPointer.getRightChild(controller);
		net.setRoot(newExecutionPointer);
	}
}
