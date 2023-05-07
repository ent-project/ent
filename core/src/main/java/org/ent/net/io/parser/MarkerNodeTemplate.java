package org.ent.net.io.parser;

import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.node.Node;

public class MarkerNodeTemplate implements NodeTemplate {

	@Override
	public Node generateNode(Net net) throws ParserException {
		return net.getMarkerNode();
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
