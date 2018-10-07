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
		result = advanceExecutionPointer(executionPointer, result);
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

		switch (executeResult) {
		case NORMAL: return StepResult.SUCCESS;
		case JUMP: return StepResult.SUCCESS_JUMP;
		case ERROR: return StepResult.COMMAND_EXECUTION_FAILED;
		default: throw new AssertionError("Unexpected execution result: " + executeResult);
		}
	}

	private StepResult advanceExecutionPointer(BNode executionPointer, StepResult result) {
		Node newExecutionPointer = executionPointer.getRightChild(controller);
		if (result == StepResult.SUCCESS_JUMP) {
			if (!(newExecutionPointer instanceof BNode)) {
				return StepResult.FATAL;
			}
			newExecutionPointer = ((BNode) newExecutionPointer).getRightChild(controller);
		}
		net.setRoot(newExecutionPointer);
		return result;
	}
}
