package org.ent.net.node;

import com.google.common.annotations.VisibleForTesting;
import org.ent.Profile;
import org.ent.net.AccessToken;
import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.veto.Veto;

import java.util.List;

/**
 * Binary node.
 */
public class Node {

	private Net net;
	private Hub hub;
	private int index;

	private int value;
	private Hub leftChildHub;
	private Hub rightChildHub;

	private final Arrow leftArrow = new BNodeLeftArrow();
	private final Arrow rightArrow = new BNodeRightArrow();
	private final List<Arrow> arrows = List.of(leftArrow, rightArrow);

	public Node(Net net, int value, Node leftChild, Node rightChild) {
		this.net = net;
		this.hub = new Hub(this);
		this.value = value;
		initialize(leftChild, rightChild);
	}

	public Node(Net net, Node leftChild, Node rightChild) {
		this(net, 0, leftChild, rightChild);
	}

	public Node(Net net, Node leftChild) {
		this.net = net;
		this.hub = new Hub(this);
		initialize(leftChild, this);
	}

	public Node(Net net) {
		this.net = net;
		this.hub = new Hub(this);
		initialize(this, this);
	}

	public Node(Net net, int value) {
		this.net = net;
		this.hub = new Hub(this);
		this.value = value;
		initialize(this, this);
	}

	private void initialize(Node leftChild, Node rightChild) {
		this.leftChildHub = leftChild.getHub();
		this.rightChildHub = rightChild.getHub();
	}

	public Net getNet() {
		return net;
	}

	public void setNet(Net net) {
		this.net = net;
	}

	public Hub getHub() {
		return hub;
	}

	public void setHub(Hub hub) {
		this.hub = hub;
	}

	public boolean permittedToSetValue(AccessToken accessToken) {
		return net.isPermittedToWrite(accessToken);
	}

	@VisibleForTesting
	public Node getLeftChild() {
		Profile.verifyTestProfile();
		return getLeftChild(Purview.DIRECT);
	}

	public Node getChild(ArrowDirection direction, Purview purview) {
		return switch (direction) {
			case LEFT -> getLeftChild(purview);
			case RIGHT -> getRightChild(purview);
		};
	}

	public boolean hasProperLeftChild() {
		return getLeftChild(Purview.DIRECT) != this;
	}

	public boolean hasProperRightChild() {
		return getRightChild(Purview.DIRECT) != this;
	}

	public void setChild(ArrowDirection direction, Node child, Purview purview) {
		switch (direction) {
			case LEFT -> setLeftChild(child, purview);
			case RIGHT -> setRightChild(child, purview);
		}
	}

	@VisibleForTesting
	public void setLeftChild(Node child) {
		Profile.verifyTestProfile();
		setLeftChild(child, Purview.DIRECT);
	}

	@VisibleForTesting
	public Node getRightChild() {
		Profile.verifyTestProfile();
		return getRightChild(Purview.DIRECT);
	}

	@VisibleForTesting
	public void setRightChild(Node child) {
		Profile.verifyTestProfile();
		setRightChild(child, Purview.DIRECT);
	}

	public int getValue() {
		Profile.verifyTestProfile();
		return getValue(Purview.DIRECT);
	}

	public void setCommand(Command command) {
		setValue(command.getValue());
	}

	public void setVeto(Veto value) {
		setValue(value.getValue());
	}

	public Command getCommand() {
		return Commands.getByValue(getValue());
	}

	public long getAddress() {
		long index = getIndex();
		long netIndex = getNet().getNetIndex();
		return index + (netIndex << 32);

		// FIXME unit tests
	}

	public Node getLeftChild(Purview purview) {
		return leftArrow.getTarget(purview);
	}

	public void setLeftChild(Node child, Purview purview) {
		leftArrow.setTarget(child, purview);
	}

	public Node getRightChild(Purview purview) {
		return rightArrow.getTarget(purview);
	}

	public void setRightChild(Node child, Purview purview) {
		rightArrow.setTarget(child, purview);
	}

	public Arrow getLeftArrow() {
		return leftArrow;
	}

	public Arrow getRightArrow() {
		return rightArrow;
	}

	public List<Arrow> getArrows() {
		return arrows;
	}

	public Arrow getArrow(ArrowDirection arrowDirection) {
		return switch (arrowDirection) {
			case LEFT -> leftArrow;
			case RIGHT -> rightArrow;
		};
	}

	final public int getValue(Purview purview) {
		if (purview != Purview.DIRECT) {
			net.event().getValue(this, purview);
		}
		return value;
	}

	public void setValue(int value) {
		net.event().setValue(this, this.value, value);
		this.value = value;
	}

	public boolean isUnaryNode() {
		return rightChildHub.getNode() == this && leftChildHub.getNode() != this;
	}

	public boolean isCommandNode() {
		return leftChildHub.getNode() == this && rightChildHub.getNode() == this;
	}

	public boolean isMarkerNode() {
		return false;
	}

	public NodeType getNodeType() {
		if (rightChildHub.getNode() == this) {
			if (leftChildHub.getNode() == this) {
				return NodeType.COMMAND_NODE;
			} else {
				return NodeType.UNARY_NODE;
			}
		} else {
			return NodeType.BINARY_NODE;
		}
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}


	private class BNodeLeftArrow implements Arrow {
		@Override
		public ArrowDirection getDirection() {
			return ArrowDirection.LEFT;
		}

		@Override
		public Node getOrigin() {
			return Node.this;
		}

		public Node getTarget(Purview purview) {
			net.fireGetTargetCall(Node.this, ArrowDirection.LEFT, purview);
			return doGetTarget();
		}

		public void setTarget(Node target, Purview purview) {
			net.fireSetTargetCall(Node.this, ArrowDirection.LEFT, target, purview);
			doSetTarget(target);
		}

		@Override
		public boolean permittedToSetTarget(Node target, AccessToken accessToken) {
			return net.isPermittedToWrite(accessToken) && target.getNet() == net;
		}

		private Node doGetTarget() {
			return leftChildHub.getNode();
		}

		private void doSetTarget(Node target) {
			leftChildHub = target.getHub();
		}
	}

	private class BNodeRightArrow implements Arrow {
		@Override
		public ArrowDirection getDirection() {
			return ArrowDirection.RIGHT;
		}

		@Override
		public Node getOrigin() {
			return Node.this;
		}

		public Node getTarget(Purview purview) {
			net.fireGetTargetCall(Node.this, ArrowDirection.RIGHT, purview);
			return doGetTarget();
		}

		public void setTarget(Node target, Purview purview) {
			net.fireSetTargetCall(Node.this, ArrowDirection.RIGHT, target, purview);
			doSetTarget(target);
		}

		@Override
		public boolean permittedToSetTarget(Node target, AccessToken accessToken) {
			return net.isPermittedToWrite(accessToken) && target.getNet() == net;
		}

		private Node doGetTarget() {
			return rightChildHub.getNode();
		}

		private void doSetTarget(Node target) {
			rightChildHub = target.getHub();
		}
	}
}
