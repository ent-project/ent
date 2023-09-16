package org.ent.net.node;

import org.ent.permission.Permissions;
import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;

import java.util.Collections;
import java.util.List;

public class MarkerNode extends Node {


	public static final String MARKER_NODE_SYMBOL = "@";

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
	public Node getLeftChild(Permissions permissions) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLeftChild(Node child, Permissions permissions) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Node getRightChild() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRightChild(Node child, Permissions permissions) {
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
	public NodeType getNodeType() {
		return NodeType.MARKER_NODE;
	}
}
