package org.ent.net.io.parser;

import org.ent.permission.Permissions;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.node.Node;

class UnaryNodeTemplate implements NodeTemplate {
	private final int value;
    private NodeTemplate child;

    public UnaryNodeTemplate(int value, NodeTemplate child) {
		this.value = value;
        this.child = child;
    }

	@Override
	public Node generateNode(Net net) {
		return net.newNode(value, Permissions.DIRECT);
	}

	@Override
	public NodeTemplate getChild(ArrowDirection arrowDirection) {
    	return switch (arrowDirection) {
			case LEFT -> child;
			case RIGHT -> this;
    	};
	}

	@Override
	public void setChild(ArrowDirection arrowDirection, NodeTemplate child) {
		switch (arrowDirection) {
			case LEFT -> this.child  = child;
			case RIGHT -> throw new IllegalArgumentException();
		}
	}

}