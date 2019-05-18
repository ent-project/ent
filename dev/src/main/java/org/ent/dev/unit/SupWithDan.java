package org.ent.dev.unit;

public class SupWithDan implements Sup {

	private final Sup supDelegate;

	private final Dan danDelegate;

	public SupWithDan(Sup supDelegate, Dan danDelegate) {
		this.supDelegate = supDelegate;
		this.danDelegate = danDelegate;
		supDelegate.setDownstream(danDelegate);
		danDelegate.setUpstream(supDelegate);
	}

	@Override
	public void setDownstream(Req downstream) {
		danDelegate.setDownstream(downstream);
	}

	@Override
	public void requestNext() {
		danDelegate.requestNext();
	}

	public Sup getSupDelegate() {
		return supDelegate;
	}

	public Dan getDanDelegate() {
		return danDelegate;
	}

}
