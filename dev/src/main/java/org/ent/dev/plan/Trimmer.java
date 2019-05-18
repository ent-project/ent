package org.ent.dev.plan;

import org.ent.dev.RunSetup;
import org.ent.dev.plan.DataProperties.PropNet;
import org.ent.dev.trim.NetTrimmer;
import org.ent.dev.unit.Data;
import org.ent.dev.unit.Proc;
import org.ent.net.Net;

public class Trimmer implements Proc {

	private final RunSetup runSetup;

	public Trimmer(RunSetup runSetup) {
		this.runSetup = runSetup;
	}

	@Override
	public void accept(Data data) {
		Net net = ((PropNet) data).getNet();
		NetTrimmer trimmer = new NetTrimmer(net, getRunSetup());
		trimmer.runTrimmer();
	}

	private RunSetup getRunSetup() {
		return runSetup;
	}
}
