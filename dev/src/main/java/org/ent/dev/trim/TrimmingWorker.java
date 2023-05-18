package org.ent.dev.trim;

import org.ent.Ent;
import org.ent.dev.ManagedRun;
import org.ent.dev.RunSetup;
import org.ent.net.Arrow;

class TrimmingWorker {

	private final Ent ent;

	private final RunSetup runSetup;

	private TrimmingExecutionEventListener evaluator;

	public TrimmingWorker(Ent ent, RunSetup runSetup) {
		this.ent = ent;
		this.runSetup = runSetup;
		this.evaluator = new TrimmingExecutionEventListener();
	}

	public void runTrimmer() {
		ent.getNet().withExecutionEventListener(evaluator, () -> {
			ManagedRun run = new ManagedRun(runSetup).withEnt(ent);
			run.perform();
		});
	}

	public boolean isDead(Arrow arrow) {
		return evaluator.isDead(arrow);
	}

}