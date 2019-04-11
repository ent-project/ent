package org.ent.dev;

public class RunSetup {

	private Integer maxSteps;

	private boolean commandExecutionFailedIsFatal;

	private boolean invalidCommandBranchIsFatal;

	private boolean invalidCommandNodeIsFatal;

	public RunSetup() {
	}

	public boolean isCommandExecutionFailedIsFatal() {
		return commandExecutionFailedIsFatal;
	}

	public void setCommandExecutionFailedIsFatal(boolean commandExecutionFailedIsFatal) {
		this.commandExecutionFailedIsFatal = commandExecutionFailedIsFatal;
	}

	public boolean isInvalidCommandBranchIsFatal() {
		return invalidCommandBranchIsFatal;
	}

	public void setInvalidCommandBranchIsFatal(boolean invalidCommandBranchIsFatal) {
		this.invalidCommandBranchIsFatal = invalidCommandBranchIsFatal;
	}

	public boolean isInvalidCommandNodeIsFatal() {
		return invalidCommandNodeIsFatal;
	}

	public void setInvalidCommandNodeIsFatal(boolean invalidCommandNodeIsFatal) {
		this.invalidCommandNodeIsFatal = invalidCommandNodeIsFatal;
	}

	public Integer getMaxSteps() {
		return maxSteps;
	}

	public void setMaxSteps(Integer maxSteps) {
		this.maxSteps = maxSteps;
	}

}
