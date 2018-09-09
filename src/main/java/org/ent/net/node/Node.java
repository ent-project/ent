package org.ent.net.node;

import java.util.List;
import java.util.Optional;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;

public abstract class Node {

	private Hub hub;

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
