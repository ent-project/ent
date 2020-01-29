package org.ent.dev.unit.combine;

import org.ent.dev.unit.Req;
import org.ent.dev.unit.Sup;
import org.ent.dev.unit.data.Data;
import org.ent.dev.unit.local.Sink;

public class SinkReq implements Req {

	private final Sink delegate;

	private Sup upstream;

	public SinkReq(Sink delegate) {
		this.delegate = delegate;
	}

	@Override
	public void setUpstream(Sup upstream) {
		this.upstream = upstream;
	}

	public void poll() {
		upstream.requestNext();
	}

	@Override
	public void receiveNext(Data next) {
		delegate.receive(next);
	}

}
