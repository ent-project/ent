package org.ent.dev;

import org.ent.Ent;
import org.ent.net.Net;
import org.ent.net.util.NetCopy;

public class CopyReplicator implements NetReplicator {

	private final Ent original;

	public CopyReplicator(Ent original) {
		this.original = original;
	}

	@Override
	public Net getNewSpecimen() {
		NetCopy copy = new NetCopy(original.getNet()); // FIXME
		return copy.createCopy();
	}

}