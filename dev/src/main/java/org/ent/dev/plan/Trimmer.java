package org.ent.dev.plan;

import org.ent.Ent;
import org.ent.dev.RunSetup;
import org.ent.dev.trim.NetTrimmer;
import org.ent.dev.unit.local.TypedProc;

public class Trimmer extends TypedProc<TrimmerData> {

	private final RunSetup runSetup;

	public Trimmer(RunSetup runSetup) {
		super(TrimmerData.class);
		this.runSetup = runSetup;
	}

	@Override
	public void doAccept(TrimmerData data) {
		Ent ent = data.getEnt();
		NetTrimmer trimmer = new NetTrimmer(ent, getRunSetup());
		trimmer.runTrimmer();
	}

	private RunSetup getRunSetup() {
		return runSetup;
	}
}