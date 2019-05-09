package org.ent.dev;

import org.ent.net.Net;

public class StepsExam {

	private final RunSetup runSetup;

	public static class StepsExamResult {

		private final StepsExam exam;

		private final int steps;

		public StepsExamResult(StepsExam exam, int steps) {
			this.exam = exam;
			this.steps = steps;
		}

		public int getSteps() {
			return steps;
		}

		public StepsExam getExam() {
			return exam;
		}
	}

	public StepsExam(RunSetup runSetup) {
		this.runSetup = runSetup;
	}

	public StepsExamResult examine(Net net) {
		ManagedRun run = new ManagedRun(runSetup).withNet(net);
		run.perform();
		int steps = run.getNoSteps();
		return new StepsExamResult(this, steps);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((runSetup == null) ? 0 : runSetup.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StepsExam other = (StepsExam) obj;
		if (runSetup == null) {
			if (other.runSetup != null)
				return false;
		} else if (!runSetup.equals(other.runSetup))
			return false;
		return true;
	}

}
