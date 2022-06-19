package org.ent.run;

import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.BNode;
import org.ent.net.node.CNode;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetRunner {

	private static final Logger log = LoggerFactory.getLogger(NetRunner.class);

	private final Net net;

	private NetRunnerListener netRunnerListener;

	public NetRunner(Net net) {
		this.net = net;
	}

	public Net getNet() {
		return net;
	}

	public NetRunnerListener getNetRunnerListener() {
		return netRunnerListener;
	}

	public void setNetRunnerListener(NetRunnerListener netRunnerListener) {
		this.netRunnerListener = netRunnerListener;
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
		if (!(executionPointer.getLeftChild(Purview.RUNNER) instanceof BNode commandBranch)) {
			return StepResult.INVALID_COMMAND_BRANCH;
		}
		if (!(commandBranch.getLeftChild(Purview.RUNNER) instanceof CNode commandNode)) {
			return StepResult.INVALID_COMMAND_NODE;
		}
		Command command = commandNode.getCommand();
		Node parameters = commandBranch.getRightChild(Purview.RUNNER);

		ExecutionResult executeResult = command.execute(parameters);
		StepResult stepResult = convertToStepResult(executeResult);
		log.trace("command {} executed: {}", command, executeResult);
		if (netRunnerListener != null) {
			netRunnerListener.fireCommandExecuted(commandNode, executeResult);
		}
		return stepResult;
	}

	private StepResult convertToStepResult(ExecutionResult executeResult) throws AssertionError {
		return switch (executeResult) {
			case NORMAL -> StepResult.SUCCESS;
			case ERROR -> StepResult.COMMAND_EXECUTION_FAILED;
		};
	}

	private void advanceExecutionPointer(BNode executionPointer) {
		Node newExecutionPointer = executionPointer.getRightChild(Purview.RUNNER);
		net.setRoot(newExecutionPointer);
	}
}
