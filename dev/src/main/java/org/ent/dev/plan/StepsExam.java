package org.ent.dev.plan;

import org.ent.dev.ManagedRun;
import org.ent.dev.RunSetup;
import org.ent.dev.unit.TypedProc;
import org.ent.net.Net;

public class StepsExam extends TypedProc<StepsExamData> {

	private final RunSetup runSetup;

	public StepsExam(RunSetup runSetup) {
		super(new StepsExamData());
		this.runSetup = runSetup;
	}

	public RunSetup getRunSetup() {
		return runSetup;
	}

	@Override
	protected void doAccept(StepsExamData data) {
		Net netExamSpecimen = data.getReplicator().getNewSpecimen();
		StepsExamResult result = examine(netExamSpecimen);

		data.setStepsExamResult(result);
	}


	public StepsExamResult examine(Net net) {
		ManagedRun run = new ManagedRun(runSetup).withNet(net);
		run.perform();
		int steps = run.getNoSteps();
		return new StepsExamResult(runSetup, steps);
	}

}
