package org.ent.net.node;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;

import java.util.List;
import java.util.Optional;

public abstract class Node {

	protected Net net;

	private Hub hub;

	protected Node(Net net) {
		this.net = net;
		this.hub = new Hub(this);
	}

	public Net getNet() {
		return net;
	}

	public void setNet(Net net) {
		this.net = net;
	}

	public Hub getHub() {
		return hub;
	}

	public void setHub(Hub hub) {
		this.hub = hub;
	}

	public abstract List<Arrow> getArrows();

	public abstract Arrow getArrow(ArrowDirection arrowDirection);

	public abstract Optional<Arrow> getArrowMaybe(ArrowDirection arrowDirection);

	/**
	 * An internal node has child nodes (as opposed to a leaf node).
	 */
	public abstract boolean isInternal();

}
