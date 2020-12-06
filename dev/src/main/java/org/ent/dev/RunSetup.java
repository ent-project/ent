package org.ent.dev;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class RunSetup {

	private final Integer maxSteps;

	private final boolean commandExecutionFailedIsFatal;

	private final boolean invalidCommandBranchIsFatal;

	private final boolean invalidCommandNodeIsFatal;

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

	public RunSetup(Integer maxSteps, boolean commandExecutionFailedIsFatal, boolean invalidCommandBranchIsFatal,
			boolean invalidCommandNodeIsFatal) {
		this.maxSteps = maxSteps;
		this.commandExecutionFailedIsFatal = commandExecutionFailedIsFatal;
		this.invalidCommandBranchIsFatal = invalidCommandBranchIsFatal;
		this.invalidCommandNodeIsFatal = invalidCommandNodeIsFatal;
	}

	public static RunSetup create(UnaryOperator<Builder> consumer) {
		return consumer.apply(new Builder()).build();
	}

	public boolean isCommandExecutionFailedIsFatal() {
		return commandExecutionFailedIsFatal;
	}

	public boolean isInvalidCommandBranchIsFatal() {
		return invalidCommandBranchIsFatal;
	}

	public boolean isInvalidCommandNodeIsFatal() {
		return invalidCommandNodeIsFatal;
	}

	public Integer getMaxSteps() {
		return maxSteps;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (commandExecutionFailedIsFatal ? 1231 : 1237);
		result = prime * result + (invalidCommandBranchIsFatal ? 1231 : 1237);
		result = prime * result + (invalidCommandNodeIsFatal ? 1231 : 1237);
		result = prime * result + ((maxSteps == null) ? 0 : maxSteps.hashCode());
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
		RunSetup other = (RunSetup) obj;
		if (commandExecutionFailedIsFatal != other.commandExecutionFailedIsFatal)
			return false;
		if (invalidCommandBranchIsFatal != other.invalidCommandBranchIsFatal)
			return false;
		if (invalidCommandNodeIsFatal != other.invalidCommandNodeIsFatal)
			return false;
		if (maxSteps == null) {
			if (other.maxSteps != null)
				return false;
		} else if (!maxSteps.equals(other.maxSteps))
			return false;
		return true;
	}

}
