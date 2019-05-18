package org.ent.dev.plan;

import org.ent.dev.plan.DataProperties.PropStepsExamResult;
import org.ent.dev.unit.Data;
import org.ent.dev.unit.Filter;

public class StepsFilter implements Filter {

	private final int minSteps;

	public StepsFilter(int minSteps) {
		this.minSteps = minSteps;
	}

	@Override
	public boolean test(Data data) {
		StepsExamResult stepsExamResult = ((PropStepsExamResult) data).getStepsExamResult();
		int steps = stepsExamResult.getSteps();
		Integer maxSteps = stepsExamResult.getRunSetup().getMaxSteps();
		return steps >= minSteps && (maxSteps == null || steps < maxSteps);
	}

}
