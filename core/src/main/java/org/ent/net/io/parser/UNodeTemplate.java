package org.ent.net.io.parser;

import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.node.Node;

class UNodeTemplate implements NodeTemplate {
    private final NodeTemplate child;

    public UNodeTemplate(NodeTemplate child) {
        this.child = child;
    }

	@Override
	public Node generateNode(Net net) {
		return net.newUNode();
	}

	@Override
	public NodeTemplate getChild(ArrowDirection arrowDirection) {
    	switch (arrowDirection) {
    	case DOWN: return child;
    	case LEFT:
    	case RIGHT: throw new IllegalArgumentException();
    	default: throw new AssertionError();
    	}
	}
}