package org.ent.net;

import org.ent.ExecutionEventHandler;
import org.ent.net.node.BNode;
import org.ent.net.node.Hub;
import org.ent.net.node.Node;
import org.ent.net.node.UNode;

public class DefaultNetController implements NetController {

	private final ExecutionEventHandler executionContext;

	private final Net net;

	public DefaultNetController(ExecutionEventHandler executionContext, Net net) {
		this.executionContext = executionContext;
		this.net = net;
	}

	@Override
	public Node getTarget(Arrow arrow) {
		if (executionContext != null) {
			executionContext.fireGetChild(arrow.getOrigin(), arrow.getType());
		}
		return arrow.getTargetForNetControllerOnly();
	}

	@Override
	public void setTarget(Arrow arrow, Node target) {
		if (executionContext != null) {
			executionContext.fireSetChild(arrow.getOrigin(), arrow.getType(), target);
		}
		arrow.setTargetForNetControllerOnly(target);
	}

	@Override
	public UNode newUNode(Node child) {
		UNode unaryNode = new UNode(child);
		if (executionContext != null) {
			executionContext.fireNewNode(unaryNode);
		}
		net.addInternalNode(unaryNode);
		return unaryNode;
	}

	@Override
	public BNode newBNode(Node leftChild, Node rightChild) {
		BNode binaryNode = new BNode(leftChild, rightChild);
		if (executionContext != null) {
			executionContext.fireNewNode(binaryNode);
		}
		net.addInternalNode(binaryNode);
		return binaryNode;
	}

	@Override
	public void referenceSwap(Node x, Node y) {
		Hub xHub = x.getHub();
		Hub yHub = y.getHub();
		x.setHub(yHub);
		yHub.setNode(x);
		y.setHub(xHub);
		xHub.setNode(y);
	}

}
