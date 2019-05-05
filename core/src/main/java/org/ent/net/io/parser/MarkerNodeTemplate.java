package org.ent.net.io.parser;

import org.ent.net.ArrowDirection;
import org.ent.net.NetController;
import org.ent.net.node.MarkerNode;
import org.ent.net.node.Node;

public class MarkerNodeTemplate implements NodeTemplate {

	private final MarkerNode markerNode;

	public MarkerNodeTemplate(MarkerNode markerNode) {
		this.markerNode = markerNode;
	}

	@Override
	public Node generateNode(NetController controller) throws ParserException {
		return markerNode;
	}

	@Override
	public NodeTemplate getChild(ArrowDirection arrowDirection) {
		throw new UnsupportedOperationException();
	}

}
