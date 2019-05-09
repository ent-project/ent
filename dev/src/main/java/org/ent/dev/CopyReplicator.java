package org.ent.dev;

import org.ent.net.Net;
import org.ent.net.util.NetCopy;

public class CopyReplicator implements NetReplicator {

	private final Net originalNet;

	public CopyReplicator(Net original) {
		this.originalNet = original;
	}

	@Override
	public Net getNewSpecimen() {
		NetCopy copy = new NetCopy(originalNet);
		return copy.createCopy();
	}

}