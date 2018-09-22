package org.ent.net.io.parser;

import org.ent.net.ArrowDirection;
import org.ent.net.NetController;
import org.ent.net.node.BNode;
import org.ent.net.node.Node;

class BNodeTemplate implements NodeTemplate {
    private final NodeTemplate leftChild;
    private final NodeTemplate rightChild;

    public BNodeTemplate(NodeTemplate leftChild, NodeTemplate rightChild) {
        this.leftChild = leftChild;
        this.rightChild = rightChild;
    }

    @Override
    public NodeTemplate getChild(ArrowDirection arrowDirection) {
    	switch (arrowDirection) {
    	case LEFT: return leftChild;
    	case RIGHT: return rightChild;
    	case DOWN: throw new IllegalArgumentException();
    	default: throw new AssertionError();
    	}
    }

	@Override
	public BNode generateNode(NetController controller, Node childPlaceholder) {
		return controller.newBNode(childPlaceholder, childPlaceholder);
	}
}