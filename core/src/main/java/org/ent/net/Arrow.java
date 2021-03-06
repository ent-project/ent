package org.ent.net;

import org.ent.net.node.Node;

/**
 * An arrow is a pair consisting of a node (that may have children) and the {@link ArrowDirection} ("child index").
 *
 * The target of the arrow is not explicitly part of this information, but can be resolved
 * at any given time ({@link #getTarget()}).
 */
public interface Arrow {

	ArrowDirection getDirection();

	Node getOrigin();

	Node getTarget(Manner manner);

	void setTarget(Node target, Manner manner);

}
