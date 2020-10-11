package org.ent.dev;

public class DefaultTestRunSetup {

    public final static RunSetup RUN_SETUP = new RunSetup.Builder()
            .withCommandExecutionFailedIsFatal(true)
            .withInvalidCommandBranchIsFatal(true)
            .withInvalidCommandNodeIsFatal(true)
            .withMaxSteps(20)
            .build();

}
