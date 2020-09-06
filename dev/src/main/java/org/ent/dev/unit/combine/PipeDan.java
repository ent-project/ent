package org.ent.dev.unit.combine;

import org.ent.dev.unit.Dan;
import org.ent.dev.unit.Req;
import org.ent.dev.unit.Sup;
import org.ent.dev.unit.data.Data;
import org.ent.dev.unit.local.Pipe;

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
		Data result = delegate.apply(next);
		downstream.deliver(result);
	}

}
