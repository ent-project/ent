package org.ent.dev;

import org.ent.Ent;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.run.EntRunner;
import org.ent.run.StepResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagedRun {

	private static final Logger log = LoggerFactory.getLogger(ManagedRun.class);

	private int noSteps;

	private final RunSetup runSetup;

	private EntRunner entRunner;

	private Ent ent;

	private NetFormatter formatter = new NetFormatter();

	private StepResult lastStepResult;

	private enum EvaluationStepResult { CONTINUE, STOP }

	public ManagedRun(RunSetup runSetup) {
		this.runSetup = runSetup;
	}

	public EntRunner getNetRunner() {
		return entRunner;
	}

	public void setNetRunner(EntRunner entRunner) {
		this.entRunner = entRunner;
	}

	public ManagedRun withNetRunner(EntRunner entRunner) {
		setNetRunner(entRunner);
		return this;
	}

	public void setEnt(Ent ent) {
		this.ent = ent;
	}

	public ManagedRun withEnt(Ent ent) {
		setEnt(ent);
		return this;
	}

	public int getNoSteps() {
		return noSteps;
	}

	public StepResult getLastStepResult() {
		return lastStepResult;
	}

	public void perform() {
		initializeEvaluation();
		EvaluationStepResult evaluationStepResult;

		do {
			evaluationStepResult = doStep();
			if (log.isTraceEnabled()) {
				log.trace("step {}/{}: {}", noSteps, evaluationStepResult, formatter.format(ent));
			}
		} while (evaluationStepResult == EvaluationStepResult.CONTINUE);
	}

	private void initializeEvaluation() {
		if (ent == null && entRunner == null) {
			throw new IllegalArgumentException("Either net or netRunner must be set.");
		}
		if (ent == null) {
			ent = entRunner.getEnt();
		}
		if (entRunner == null) {
			entRunner = new EntRunner(ent);
		}
		noSteps = 0;
		if (log.isTraceEnabled()) {
			log.trace("before: {}", formatter.format(ent));
		}
	}

	private EvaluationStepResult doStep() {
		StepResult result = entRunner.step();
		boolean isFatal = isStepResultFatal(result);
		if (isFatal) {
			this.lastStepResult = result;
			return EvaluationStepResult.STOP;
		}
		noSteps++;
		if (runSetup.maxSteps() != null && noSteps >= runSetup.maxSteps()) {
			log.debug("Max steps {} exceeded - end evaluation.", runSetup.maxSteps());
			return EvaluationStepResult.STOP;
		}
		return EvaluationStepResult.CONTINUE;
	}

	private boolean isStepResultFatal(StepResult result) throws AssertionError {
		return switch (result) {
			case SUCCESS -> false;
			case FATAL, ENDLESS_LOOP -> true;
			case COMMAND_EXECUTION_FAILED -> {
				if (runSetup.commandExecutionFailedIsFatal()) {
					log.debug("Command execution failed in step {} - end evaluation", noSteps);
					yield true;
				} else {
					yield false;
				}
			}
			case INVALID_COMMAND_NODE -> {
				if (runSetup.invalidCommandNodeIsFatal()) {
					log.debug("Invalid command node in step {} - end evaluation", noSteps);
					yield true;
				} else {
					yield false;
				}
			}
		};
	}

	void setFormatter(NetFormatter formatter) {
		this.formatter = formatter;
	}

	ManagedRun withFormatter(NetFormatter formatter) {
		setFormatter(formatter);
		return this;
	}

}
