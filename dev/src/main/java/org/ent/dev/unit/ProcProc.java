package org.ent.dev.unit;

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
