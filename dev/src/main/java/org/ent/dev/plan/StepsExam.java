package org.ent.dev.plan;

import org.ent.dev.ManagedRun;
import org.ent.dev.RunSetup;
import org.ent.dev.plan.DataProperties.PropReplicator;
import org.ent.dev.plan.DataProperties.PropStepsExamResult;
import org.ent.dev.unit.Data;
import org.ent.dev.unit.Proc;
import org.ent.net.Net;

public class StepsExam implements Proc {

	private final RunSetup runSetup;

	public StepsExam(RunSetup runSetup) {
		this.runSetup = runSetup;
	}

	public RunSetup getRunSetup() {
		return runSetup;
	}

	@Override
	public void accept(Data data) {
		Net netExamSpecimen = ((PropReplicator) data).getReplicator().getNewSpecimen();
		StepsExamResult result = examine(netExamSpecimen);

		((PropStepsExamResult) data).setStepsExamResult(result);
	}


	public StepsExamResult examine(Net net) {
		ManagedRun run = new ManagedRun(runSetup).withNet(net);
		run.perform();
		int steps = run.getNoSteps();
		return new StepsExamResult(runSetup, steps);
	}

}
