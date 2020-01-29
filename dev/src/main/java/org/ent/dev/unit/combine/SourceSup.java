package org.ent.dev.unit.combine;

import org.ent.dev.unit.Req;
import org.ent.dev.unit.Sup;
import org.ent.dev.unit.data.Data;
import org.ent.dev.unit.local.Source;

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
