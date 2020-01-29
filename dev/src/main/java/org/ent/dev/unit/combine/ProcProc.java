package org.ent.dev.unit.combine;

import org.ent.dev.unit.data.Data;
import org.ent.dev.unit.local.Proc;

public class ProcProc implements Proc {

	private final Proc delegateUp;

	private final Proc delegateDown;

	public ProcProc(Proc delegateUp, Proc delegateDown) {
		this.delegateUp = delegateUp;
		this.delegateDown = delegateDown;
	}

	@Override
	public void accept(Data data) {
		delegateUp.accept(data);
		delegateDown.accept(data);
	}

}
