package org.ent.net;

import org.ent.ExecutionEventHandler;
import org.ent.net.node.BNode;
import org.ent.net.node.CNode;
import org.ent.net.node.Hub;
import org.ent.net.node.Node;
import org.ent.net.node.UNode;
import org.ent.net.node.cmd.Command;

public class DefaultNetController implements NetController {

	private final Net net;

	private final ExecutionEventHandler executionContext;

	public DefaultNetController(Net net) {
		this(net, null);
	}

	public DefaultNetController(Net net, ExecutionEventHandler executionContext) {
		this.net = net;
		this.executionContext = executionContext;
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
		net.addNode(unaryNode);
		return unaryNode;
	}

	@Override
	public BNode newBNode(Node leftChild, Node rightChild) {
		BNode binaryNode = new BNode(leftChild, rightChild);
		if (executionContext != null) {
			executionContext.fireNewNode(binaryNode);
		}
		net.addNode(binaryNode);
		return binaryNode;
	}

	@Override
	public CNode newCNode(Command command) {
		CNode commandNode = new CNode(command);
		if (executionContext != null) {
			executionContext.fireNewNode(commandNode);
		}
		net.addNode(commandNode);
		return commandNode;
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
