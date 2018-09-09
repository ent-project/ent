package org.ent.net;

import org.ent.net.node.Node;

public interface Arrow {

	ArrowDirection getType();

	Node getOrigin();

	default Node getTarget(NetController nc) {
		return nc.getTarget(this);
	}

	default void setTarget(NetController nc, Node target) {
		nc.setTarget(this, target);
	}

	Node getTargetForNetControllerOnly();

	void setTargetForNetControllerOnly(Node target);
}
