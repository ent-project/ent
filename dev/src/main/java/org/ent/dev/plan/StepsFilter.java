package org.ent.dev.plan;

import org.ent.dev.unit.TypedFilter;

public class StepsFilter extends TypedFilter<StepsFilterData> {

	private final int minSteps;

	public StepsFilter(int minSteps) {
		super(new StepsFilterData());
		this.minSteps = minSteps;
	}

	@Override
	public boolean doTest(StepsFilterData data) {
		StepsExamResult stepsExamResult = data.getStepsExamResult();
		int steps = stepsExamResult.getSteps();
		Integer maxSteps = stepsExamResult.getRunSetup().getMaxSteps();
		return steps >= minSteps && (maxSteps == null || steps < maxSteps);
	}

}