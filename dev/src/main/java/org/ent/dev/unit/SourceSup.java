package org.ent.dev.unit;

public class SourceSup implements Sup {

	private Req downstream;

	private final Source delegate;

	public SourceSup(Source delegate) {
		this.delegate = delegate;
	}

	@Override
	public void setDownstream(Req downstream) {
		this.downstream = downstream;
	}

	@Override
	public void requestNext() {
		Data next = delegate.get();
		downstream.deliver(next);
	}

}
