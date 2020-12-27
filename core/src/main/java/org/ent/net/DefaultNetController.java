package org.ent.net;

import org.ent.ExecutionEventListener;
import org.ent.net.node.BNode;
import org.ent.net.node.CNode;
import org.ent.net.node.Hub;
import org.ent.net.node.MarkerNode;
import org.ent.net.node.Node;
import org.ent.net.node.UNode;
import org.ent.net.node.cmd.Command;

public class DefaultNetController implements NetController {

	private static final boolean VALIDATE = true;

	private final Net net;

	private final ExecutionEventListener eventListener;

	private final MarkerNode placeHolder = new MarkerNode();

	private ExecutionContext context;

	public DefaultNetController(Net net) {
		this(net, null);
	}

	public DefaultNetController(Net net, ExecutionEventListener eventListener) {
		this.net = net;
		this.eventListener = eventListener;
	}

	@Override
	public Node getTarget(Arrow arrow) {
		if (VALIDATE) {
			if (!net.belongsToNet(arrow.getOrigin())) {
				throw new IllegalStateException("arrow origin does not belong to controlled net");
			}
		}
		if (eventListener != null) {
			eventListener.fireGetChild(arrow.getOrigin(), arrow.getDirection(), context);
		}
		return arrow.getTargetForNetControllerOnly();
	}

	@Override
	public void setTarget(Arrow arrow, Node target) {
		if (VALIDATE) {
			if (!net.belongsToNet(arrow.getOrigin())) {
				throw new IllegalStateException("arrow origin does not belong to controlled net");
			}
			if (!net.belongsToNet(target)) {
				throw new IllegalStateException("target does not belong to controlled net");
			}
		}
		if (eventListener != null) {
			eventListener.fireSetChild(arrow.getOrigin(), arrow.getDirection(), target, context);
		}
		arrow.setTargetForNetControllerOnly(target);
	}

	@Override
	public UNode newUNode() {
		UNode unaryNode = new UNode(placeHolder);
		net.addNode(unaryNode);
		unaryNode.getArrow().setTargetForNetControllerOnly(unaryNode);
		if (eventListener != null) {
			eventListener.fireNewNode(unaryNode);
		}
		return unaryNode;
	}

	@Override
	public UNode newUNode(Node child) {
		if (VALIDATE) {
			if (!net.belongsToNet(child)) {
				throw new IllegalStateException("child does not belong to controlled net");
			}
		}
		UNode unaryNode = new UNode(child);
		if (eventListener != null) {
			eventListener.fireNewNode(unaryNode);
		}
		net.addNode(unaryNode);
		return unaryNode;
	}

	@Override
	public BNode newBNode() {
		BNode binaryNode = new BNode(placeHolder, placeHolder);
		net.addNode(binaryNode);
		binaryNode.getLeftArrow().setTargetForNetControllerOnly(binaryNode);
		binaryNode.getRightArrow().setTargetForNetControllerOnly(binaryNode);
		if (eventListener != null) {
			eventListener.fireNewNode(binaryNode);
		}
		return binaryNode;
	}

	@Override
	public BNode newBNode(Node leftChild, Node rightChild) {
		if (VALIDATE) {
			if (!net.belongsToNet(leftChild)) {
				throw new IllegalStateException("left child does not belong to controlled net");
			}
			if (!net.belongsToNet(rightChild)) {
				throw new IllegalStateException("right child does not belong to controlled net");
			}
		}
		BNode binaryNode = new BNode(leftChild, rightChild);
		if (eventListener != null) {
			eventListener.fireNewNode(binaryNode);
		}
		net.addNode(binaryNode);
		return binaryNode;
	}

	@Override
	public CNode newCNode(Command command) {
		CNode commandNode = new CNode(command);
		if (eventListener != null) {
			eventListener.fireNewNode(commandNode);
		}
		net.addNode(commandNode);
		return commandNode;
	}

	@Override
	public void ancestorSwap(Node node1, Node node2) {
		if (VALIDATE) {
			if (!net.belongsToNet(node1)) {
				throw new IllegalStateException("first node does not belong to controlled net");
			}
			if (!net.belongsToNet(node2)) {
				throw new IllegalStateException("second node does not belong to controlled net");
			}
		}
		Hub hub1 = node1.getHub();
		Hub hub2 = node2.getHub();
		node1.setHub(hub2);
		hub2.setNode(node1);
		node2.setHub(hub1);
		hub1.setNode(node2);
	}

	@Override
	public void setContext(ExecutionContext context) {
		this.context = context;
	}

}
