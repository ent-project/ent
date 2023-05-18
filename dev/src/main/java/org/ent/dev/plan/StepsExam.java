package org.ent.dev.plan;

import org.ent.Ent;
import org.ent.dev.ManagedRun;
import org.ent.dev.RunSetup;
import org.ent.dev.unit.local.TypedProc;
import org.ent.net.Net;

public class StepsExam extends TypedProc<StepsExamData> {

	private final RunSetup runSetup;

	public StepsExam(RunSetup runSetup) {
		super(StepsExamData.class);
		this.runSetup = runSetup;
	}

	public RunSetup getRunSetup() {
		return runSetup;
	}

	@Override
	protected void doAccept(StepsExamData data) {
		Net netExamSpecimen = data.getReplicator().getNewSpecimen();
		Ent ent = new Ent(netExamSpecimen); // FIXME
		StepsExamResult result = examine(ent);

		data.setStepsExamResult(result);
	}

	public StepsExamResult examine(Ent ent) {
		ManagedRun run = new ManagedRun(runSetup).withEnt(ent);
		run.perform();
		return new StepsExamResult(runSetup, run.getNoSteps(), run.getLastStepResult());
	}

}
