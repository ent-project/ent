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
	private boolean checkConclusion = true;

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

	public void setCheckConclusion(boolean checkConclusion) {
		this.checkConclusion = checkConclusion;
	}

	public EntRunnerListener getNetRunnerListener() {
		return entRunnerListener;
	}

	public void setNetRunnerListener(EntRunnerListener entRunnerListener) {
		this.entRunnerListener = entRunnerListener;
	}

	public StepResult step() {
		Node executionPointer = net.getRoot();
		StepResult result;
		Command command = Commands.getByValue(executionPointer.getValue(net.getPermissions()));
		if (command == null) {
			ent.event().beforeCommandExecution(executionPointer, null);
			result = StepResult.INVALID_COMMAND_NODE;
		} else if (checkConclusion && command.isConcluding()) {
			result = StepResult.CONCLUDED;
		} else {
			ent.event().beforeCommandExecution(executionPointer, command);
			ExecutionResult executeResult = command.execute(executionPointer, net.getPermissions());
			StepResult stepResult = convertToStepResult(executeResult);
			log.trace("command {} executed: {}", command, executeResult);
			ent.event().afterCommandExecution(stepResult);
			if (entRunnerListener != null) {
				entRunnerListener.fireCommandExecuted(executionPointer, executeResult);
			}
			result = stepResult;
		}

		if (result != StepResult.CONCLUDED) {
			Node newExecutionPointer = executionPointer.getRightChild(net.getPermissions());
			if (newExecutionPointer.getNet().equals(net)) {
				net.setRoot(newExecutionPointer);
			} else {
				result = StepResult.EXECUTION_POINTER_LEAVING_NET;
			}
		}
		return result;
	}

	private StepResult convertToStepResult(ExecutionResult executeResult) throws AssertionError {
		return switch (executeResult) {
			case NORMAL -> StepResult.SUCCESS;
			case ERROR -> StepResult.COMMAND_EXECUTION_FAILED;
		};
	}

}
