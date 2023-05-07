package org.ent.run;

import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.CommandFactory;
import org.ent.net.node.cmd.Commands;
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
		Node executionPointer = net.getRoot();
		StepResult result = doStep(executionPointer);
		advanceExecutionPointer(executionPointer);
		return result;
	}

	private StepResult doStep(Node executionPointer) {
		boolean executionPointerDoesNotAdvance = executionPointer.getRightChild(Purview.RUNNER) == executionPointer;
		Command command = CommandFactory.getByValue(executionPointer.getValue());
		if (command == null) {
			if (executionPointerDoesNotAdvance) {
				return StepResult.ENDLESS_LOOP;
			} else {
				return StepResult.INVALID_COMMAND_NODE;
			}
		}

		ExecutionResult executeResult = command.execute(executionPointer.getLeftArrow());
		StepResult stepResult = convertToStepResult(executeResult);
		log.trace("command {} executed: {}", command, executeResult);
		if (netRunnerListener != null) {
			netRunnerListener.fireCommandExecuted(executionPointer, executeResult);
		}

		if (command.getValue() == Commands.NOP.getValue() && executionPointerDoesNotAdvance) {
			return StepResult.ENDLESS_LOOP;
		}
		return stepResult;
	}

	private StepResult convertToStepResult(ExecutionResult executeResult) throws AssertionError {
		return switch (executeResult) {
			case NORMAL -> StepResult.SUCCESS;
			case ERROR -> StepResult.COMMAND_EXECUTION_FAILED;
		};
	}

	private void advanceExecutionPointer(Node executionPointer) {
		Node newExecutionPointer = executionPointer.getRightChild(Purview.RUNNER);
		net.setRoot(newExecutionPointer);
	}
}
