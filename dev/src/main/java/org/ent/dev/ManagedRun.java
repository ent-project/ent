package org.ent.dev;

import org.ent.ExecutionEventHandler;
import org.ent.net.DefaultNetController;
import org.ent.net.Net;
import org.ent.net.NetController;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.run.NetRunner;
import org.ent.run.StepResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagedRun {

	private static final Logger log = LoggerFactory.getLogger(ManagedRun.class);

	private int noSteps;

	private RunSetup runSetup;

	private NetRunner netRunner;

	private Net net;

	private ExecutionEventHandler evaluator;

	private NetFormatter formatter;

	private enum EvaluationStepResult { CONTINUE, STOP }

	public ManagedRun(RunSetup runSetup, ExecutionEventHandler evaluator) {
		this.runSetup = runSetup;
		this.evaluator = evaluator;
	}

	public ManagedRun(RunSetup runSetup) {
		this.runSetup = runSetup;
	}

	public NetRunner getNetRunner() {
		return netRunner;
	}

	public void setNetRunner(NetRunner netRunner) {
		this.netRunner = netRunner;
	}

	public ManagedRun withNetRunner(NetRunner netRunner) {
		setNetRunner(netRunner);
		return this;
	}

	public Net getNet() {
		return net;
	}

	public void setNet(Net net) {
		this.net = net;
	}

	public ManagedRun withNet(Net net) {
		setNet(net);
		return this;
	}

	public ExecutionEventHandler getEvaluator() {
		return evaluator;
	}

	public void setEvaluator(ExecutionEventHandler evaluator) {
		this.evaluator = evaluator;
	}

	public int getNoSteps() {
		return noSteps;
	}

	public void perform() {
		initializeEvaluation();
		EvaluationStepResult evaluationStepResult;

		do {
			evaluationStepResult = doStep();
			if (log.isTraceEnabled()) {
				log.trace("step {}/{}: {}", noSteps, evaluationStepResult, formatter.format(net));
			}
		} while (evaluationStepResult == EvaluationStepResult.CONTINUE);
	}

	private void initializeEvaluation() {
		if (net == null && netRunner == null) {
			throw new IllegalArgumentException("Either net or netRunner must be set.");
		}
		if (net == null) {
			net = netRunner.getNet();
		}
		NetController controller = new DefaultNetController(net, evaluator);
		if (netRunner == null) {
			netRunner = new NetRunner(net, controller);
		}
		noSteps = 0;
		if (log.isTraceEnabled()) {
			formatter = new NetFormatter();
			log.trace("before: {}", formatter.format(net));
		}
	}

	private EvaluationStepResult doStep() {
		StepResult result = netRunner.step();
		boolean isFatal = isStepResultFatal(result);
		if (isFatal) {
			return EvaluationStepResult.STOP;
		}
		noSteps++;
		if (runSetup.getMaxSteps() != null && noSteps >= runSetup.getMaxSteps()) {
			log.debug("Max steps {} exceeded - end evaluation.", runSetup.getMaxSteps());
			return EvaluationStepResult.STOP;
		}
		return EvaluationStepResult.CONTINUE;
	}

	private boolean isStepResultFatal(StepResult result) throws AssertionError {
		switch (result) {
		case SUCCESS:
			return false;
		case FATAL:
			return true;
		case COMMAND_EXECUTION_FAILED:
			if (runSetup.isCommandExecutionFailedIsFatal()) {
				log.debug("Command execution failed in step {} - end evaluation", noSteps);
				return true;
			} else {
				return false;
			}
		case INVALID_COMMAND_BRANCH:
			if (runSetup.isInvalidCommandBranchIsFatal()) {
				log.debug("Invalid command branch in step {} - end evaluation", noSteps);
				return true;
			} else {
				return false;
			}
		case INVALID_COMMAND_NODE:
			if (runSetup.isInvalidCommandNodeIsFatal()) {
				log.debug("Invalid command node in step {} - end evaluation", noSteps);
				return true;
			} else {
				return false;
			}
		default:
			throw new AssertionError("Unexpected StepResult: " + result);
		}
	}
}
