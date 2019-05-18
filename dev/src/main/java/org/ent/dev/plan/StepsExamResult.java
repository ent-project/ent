package org.ent.dev.plan;

import org.ent.dev.RunSetup;

public class StepsExamResult {

	private final RunSetup runSetup;

	private final int steps;

	public StepsExamResult(RunSetup runSetup, int steps) {
		this.runSetup = runSetup;
		this.steps = steps;
	}

	public RunSetup getRunSetup() {
		return runSetup;
	}

	public int getSteps() {
		return steps;
	}

}