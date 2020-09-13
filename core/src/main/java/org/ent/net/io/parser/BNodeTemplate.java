package org.ent.net.io.parser;

import org.ent.net.ArrowDirection;
import org.ent.net.NetController;
import org.ent.net.node.BNode;

class BNodeTemplate implements NodeTemplate {
    private final NodeTemplate leftChild;
    private final NodeTemplate rightChild;

    public BNodeTemplate(NodeTemplate leftChild, NodeTemplate rightChild) {
        this.leftChild = leftChild;
        this.rightChild = rightChild;
    }

    @Override
    public NodeTemplate getChild(ArrowDirection arrowDirection) {
    	return switch (arrowDirection) {
    		case LEFT -> leftChild;
    		case RIGHT -> rightChild;
    		case DOWN -> throw new IllegalArgumentException();
    	};
    }

	@Override
	public BNode generateNode(NetController controller) {
		return controller.newBNode();
	}
}