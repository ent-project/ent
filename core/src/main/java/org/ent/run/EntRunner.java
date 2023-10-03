package org.ent.run;

import org.ent.Ent;
import org.ent.net.Net;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntRunner {

	private static final Logger log = LoggerFactory.getLogger(EntRunner.class);

	private final Ent ent;
	private final Net net;

	private EntRunnerListener entRunnerListener;

	public EntRunner(Ent ent) {
		this.ent = ent;
		this.net = ent.getNet();
	}

	public EntRunner(Net net) {
		this(new Ent(net));
	}

	public Net getNet() {
		return net;
	}

	public Ent getEnt() {
		return ent;
	}

	public EntRunnerListener getNetRunnerListener() {
		return entRunnerListener;
	}

	public void setNetRunnerListener(EntRunnerListener entRunnerListener) {
		this.entRunnerListener = entRunnerListener;
	}

	public StepResult step() {
		Node executionPointer = net.getRoot();
		StepResult result = doStep(executionPointer);
		if (result != StepResult.CONCLUDED) {
			advanceExecutionPointer(executionPointer);
		}
		return result;
	}

	private StepResult doStep(Node executionPointer) {
		Command command = Commands.getByValue(executionPointer.getValue(net.getPermissions()));
		if (command == null) {
			ent.event().beforeCommandExecution(executionPointer, null);
			return StepResult.INVALID_COMMAND_NODE;
		} else {
			ent.event().beforeCommandExecution(executionPointer, command);
			ExecutionResult executeResult = command.execute(executionPointer, net.getPermissions());
			StepResult stepResult = convertToStepResult(executeResult);
			log.trace("command {} executed: {}", command, executeResult);
			ent.event().afterCommandExecution(stepResult);
			if (entRunnerListener != null) {
				entRunnerListener.fireCommandExecuted(executionPointer, executeResult);
			}
			return stepResult;
		}
	}

	private StepResult convertToStepResult(ExecutionResult executeResult) throws AssertionError {
		return switch (executeResult) {
			case NORMAL -> StepResult.SUCCESS;
			case ERROR -> StepResult.COMMAND_EXECUTION_FAILED;
			case CONCLUDED -> StepResult.CONCLUDED;
		};
	}

	private void advanceExecutionPointer(Node executionPointer) {
		Node newExecutionPointer = executionPointer.getRightChild(net.getPermissions());
		if (newExecutionPointer.getNet().equals(net)) {
			net.setRoot(newExecutionPointer);
		} else {
			ent.event().executionPointerTryingToLeaveNet(executionPointer, newExecutionPointer);
		}
	}

}
