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

	private final ExecutionEventHandler eventHandler;

	public DefaultNetController(Net net) {
		this(net, null);
	}

	public DefaultNetController(Net net, ExecutionEventHandler eventHandler) {
		this.net = net;
		this.eventHandler = eventHandler;
	}

	@Override
	public Node getTarget(Arrow arrow) {
		if (eventHandler != null) {
			eventHandler.fireGetChild(arrow.getOrigin(), arrow.getDirection());
		}
		return arrow.getTargetForNetControllerOnly();
	}

	@Override
	public void setTarget(Arrow arrow, Node target) {
		if (eventHandler != null) {
			eventHandler.fireSetChild(arrow.getOrigin(), arrow.getDirection(), target);
		}
		arrow.setTargetForNetControllerOnly(target);
	}

	@Override
	public UNode newUNode(Node child) {
		UNode unaryNode = new UNode(child);
		if (eventHandler != null) {
			eventHandler.fireNewNode(unaryNode);
		}
		net.addNode(unaryNode);
		return unaryNode;
	}

	@Override
	public BNode newBNode(Node leftChild, Node rightChild) {
		BNode binaryNode = new BNode(leftChild, rightChild);
		if (eventHandler != null) {
			eventHandler.fireNewNode(binaryNode);
		}
		net.addNode(binaryNode);
		return binaryNode;
	}

	@Override
	public CNode newCNode(Command command) {
		CNode commandNode = new CNode(command);
		if (eventHandler != null) {
			eventHandler.fireNewNode(commandNode);
		}
		net.addNode(commandNode);
		return commandNode;
	}

	@Override
	public void ancestorSwap(Node x, Node y) {
		Hub xHub = x.getHub();
		Hub yHub = y.getHub();
		x.setHub(yHub);
		yHub.setNode(x);
		y.setHub(xHub);
		xHub.setNode(y);
	}

}
