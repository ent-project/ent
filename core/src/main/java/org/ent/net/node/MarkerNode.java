package org.ent.net.node;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.Purview;

import java.util.Collections;
import java.util.List;

public class MarkerNode extends BNode {

	public static final String MARKER_NODE_SYMBOL = "●";

	public static final String MARKER_NODE_SYMBOL_ASCII = "@";

	public MarkerNode(Net net) {
		super(net);
	}

	@Override
    public List<Arrow> getArrows() {
    	return Collections.emptyList();
    }

	@Override
	public Arrow getArrow(ArrowDirection arrowDirection) {
        throw new UnsupportedOperationException();
	}

	@Override
	public Node getLeftChild(Purview purview) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLeftChild(Node child, Purview purview) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Node getRightChild(Purview purview) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRightChild(Node child, Purview purview) {
		throw new UnsupportedOperationException();
	}

	@Override
    public String toString() {
        return MARKER_NODE_SYMBOL;
    }

	@Override
	public boolean isMarkerNode() {
		return true;
	}

	@Override
	public boolean isUnaryNode() {
		return false;
	}

	@Override
	public boolean isCommandNode() {
		return false;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.MARKER_NODE;
	}
}
