package org.ent.dev.unit.combine;

import org.ent.dev.unit.Dan;
import org.ent.dev.unit.Req;
import org.ent.dev.unit.Sup;
import org.ent.dev.unit.data.Data;
import org.ent.dev.unit.local.Filter;

public class FilterDan implements Dan {

	private Sup upstream;

	private Req downstream;

	private final Filter delegate;

	public FilterDan(Filter delegate) {
		this.delegate = delegate;
	}

	@Override
	public void setDownstream(Req downstream) {
		this.downstream = downstream;
	}

	@Override
	public void setUpstream(Sup upstream) {
		this.upstream = upstream;
	}

	@Override
	public void requestNext() {
		upstream.requestNext();
	}

	@Override
	public void receiveNext(Data next) {
		if (delegate.test(next)) {
			downstream.deliver(next);
		} else {
			upstream.requestNext();
		}
	}

}
