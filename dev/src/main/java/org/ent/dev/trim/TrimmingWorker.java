package org.ent.dev.trim;

import org.ent.dev.ManagedRun;
import org.ent.dev.RunSetup;
import org.ent.net.Arrow;
import org.ent.net.Net;

class TrimmingWorker {

	private final Net net;

	private final RunSetup runSetup;

	private TrimmingExecutionEventListener evaluator = new TrimmingExecutionEventListener();

	public TrimmingWorker(Net net, RunSetup runSetup) {
		this.net = net;
		this.runSetup = runSetup;
		this.evaluator = new TrimmingExecutionEventListener();
	}

	public void runTrimmer() {
		ManagedRun run = new ManagedRun(runSetup, evaluator).withNet(net);
		run.perform();
	}

	public boolean isDead(Arrow arrow) {
		return evaluator.isDead(arrow);
	}

}