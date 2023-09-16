package org.ent.net;

import org.ent.permission.Permissions;
import org.ent.net.node.Node;

/**
 * An arrow is a pair consisting of a node (origin) and the {@link ArrowDirection}.
 *
 * The target of the arrow is not explicitly part of this information, but can be resolved
 * at any given time ({@link #getTarget(Permissions)}).
 */
public interface Arrow {

	Node getOrigin();

	ArrowDirection getDirection();

	Node getTarget(Permissions permissions);

	void setTarget(Node target, Permissions permissions);

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
