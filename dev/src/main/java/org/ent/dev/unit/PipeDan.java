package org.ent.dev.unit;

public class PipeDan implements Dan {

	private Req downstream;

	private Sup upstream;

	private Pipe delegate;

	public PipeDan(Pipe delegate) {
		this.delegate = delegate;
	}

	@Override
	public void setDownstream(Req downstream) {
		this.downstream = downstream;
	}

	@Override
	public void requestNext() {
		upstream.requestNext();
	}

	@Override
	public void setUpstream(Sup upstream) {
		this.upstream = upstream;
	}

	@Override
	public void receiveNext(Data next) {
		Data result = (Data) delegate.apply(next);
		downstream.deliver(result);
	}

}
