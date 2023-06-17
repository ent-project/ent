package org.ent.net;

import org.ent.net.node.Node;

/**
 * An arrow is a pair consisting of a node (that may have children) and the {@link ArrowDirection} ("child index").
 *
 * The target of the arrow is not explicitly part of this information, but can be resolved
 * at any given time ({@link #getTarget(Purview)}).
 */
public interface Arrow {

	ArrowDirection getDirection();

	Node getOrigin();

	Node getTarget(Purview purview);

	void setTarget(Node target, Purview purview);

	default void setTarget(Node target, Purview purview, AccessToken token) {
		setTarget(target, purview);
	}

	boolean permittedToSetTarget(Node target, AccessToken accessToken);

	default int getIndex() {
		Node origin = getOrigin();
		int index = origin.getIndex();
		index <<= 1;
		if (getDirection() == ArrowDirection.RIGHT) {
			index |= 1;
		}
		return index;
	}
}
