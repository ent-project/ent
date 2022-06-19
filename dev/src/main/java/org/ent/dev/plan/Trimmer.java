package org.ent.dev.plan;

import org.ent.dev.RunSetup;
import org.ent.dev.trim.NetTrimmer;
import org.ent.dev.unit.local.TypedProc;
import org.ent.net.Net;

public class Trimmer extends TypedProc<TrimmerData> {

	private final RunSetup runSetup;

	public Trimmer(RunSetup runSetup) {
		super(TrimmerData.class);
		this.runSetup = runSetup;
	}

	@Override
	public void doAccept(TrimmerData data) {
		Net net = data.getNet();
		NetTrimmer trimmer = new NetTrimmer(net, getRunSetup());
		trimmer.runTrimmer();
	}

	private RunSetup getRunSetup() {
		return runSetup;
	}
}