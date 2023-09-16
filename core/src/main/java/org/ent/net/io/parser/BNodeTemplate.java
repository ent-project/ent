package org.ent.net.io.parser;

import org.ent.permission.Permissions;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.node.Node;

class BNodeTemplate implements NodeTemplate {
    private final int value;
    private NodeTemplate leftChild;
    private NodeTemplate rightChild;

    public BNodeTemplate(int value, NodeTemplate leftChild, NodeTemplate rightChild) {
        this.value = value;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
    }

    @Override
    public NodeTemplate getChild(ArrowDirection arrowDirection) {
    	return switch (arrowDirection) {
    		case LEFT -> leftChild;
    		case RIGHT -> rightChild;
    	};
    }

    @Override
    public void setChild(ArrowDirection arrowDirection, NodeTemplate child) {
        switch (arrowDirection) {
            case LEFT -> this.leftChild  = child;
            case RIGHT -> this.rightChild = child;
        }
    }

    @Override
	public Node generateNode(Net net) {
		return net.newNode(value, Permissions.DIRECT);
	}
}