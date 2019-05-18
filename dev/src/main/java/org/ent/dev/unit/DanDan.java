package org.ent.dev.unit;

public class DanDan implements Dan {

	private Dan delegateUp;

	private Dan delegateDown;

	public DanDan(Dan delegateUp, Dan delegateDown) {
		this.delegateUp = delegateUp;
		this.delegateDown = delegateDown;
		delegateUp.setDownstream(delegateDown);
		delegateDown.setUpstream(delegateUp);

	}

	@Override
	public void setDownstream(Req downstream) {
		delegateDown.setDownstream(downstream);
	}

	@Override
	public void setUpstream(Sup upstream) {
		delegateUp.setUpstream(upstream);
	}

	@Override
	public void requestNext() {
		delegateDown.requestNext();
	}

	@Override
	public void receiveNext(Data next) {
		delegateUp.receiveNext(next);
	}

}
