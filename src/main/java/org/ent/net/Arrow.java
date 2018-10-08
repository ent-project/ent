package org.ent.net;

import org.ent.net.node.Node;

/**
 * An arrow is a pair consisting of a node (that may have children) and the {@link ArrowDirection} ("child index").
 *
 * The target of the arrow is not explicitly part of this information, but can be resolved
 * at any given time ({@link #getTarget()}).
 */
public interface Arrow {

	ArrowDirection getType();

	Node getOrigin();

	default Node getTarget(NetController controller) {
		return controller.getTarget(this);
	}

	default void setTarget(NetController controller, Node target) {
		controller.setTarget(this, target);
	}

	Node getTargetForNetControllerOnly();

	void setTargetForNetControllerOnly(Node target);
}
