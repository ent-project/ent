package org.ent.dev;

import java.util.function.UnaryOperator;

public final record RunSetup(
		Integer maxSteps,
		boolean commandExecutionFailedIsFatal,
		boolean invalidCommandBranchIsFatal,
		boolean invalidCommandNodeIsFatal) {

	public static class Builder {

		private Integer maxSteps;
		private boolean commandExecutionFailedIsFatal;
		private boolean invalidCommandBranchIsFatal;
		private boolean invalidCommandNodeIsFatal;

		public Builder withMaxSteps(Integer maxSteps) {
			this.maxSteps = maxSteps;
			return this;
		}

		public Builder withCommandExecutionFailedIsFatal(boolean commandExecutionFailedIsFatal) {
			this.commandExecutionFailedIsFatal = commandExecutionFailedIsFatal;
			return this;
		}

		public Builder withInvalidCommandBranchIsFatal(boolean invalidCommandBranchIsFatal) {
			this.invalidCommandBranchIsFatal = invalidCommandBranchIsFatal;
			return this;
		}

		public Builder withInvalidCommandNodeIsFatal(boolean invalidCommandNodeIsFatal) {
			this.invalidCommandNodeIsFatal = invalidCommandNodeIsFatal;
			return this;
		}

		public RunSetup build() {
			return new RunSetup(maxSteps, commandExecutionFailedIsFatal, invalidCommandBranchIsFatal, invalidCommandNodeIsFatal);
		}
	}

	public static RunSetup create(UnaryOperator<Builder> consumer) {
		return consumer.apply(new Builder()).build();
	}

}
