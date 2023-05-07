package org.ent.net.io.parser;

import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.node.Node;

class IdentifierNodeTemplate implements NodeTemplate {
    private final String name;

    public IdentifierNodeTemplate(String name) {
        this.name = name;
    }

	String getName() {
		return name;
	}

	@Override
	public Node generateNode(Net net) throws ParserException {
		throw new UnsupportedOperationException();
	}

	@Override
	public NodeTemplate getChild(ArrowDirection arrowDirection) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setChild(ArrowDirection arrowDirection, NodeTemplate child) {
		throw new UnsupportedOperationException();
	}
}