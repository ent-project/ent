package org.ent.net.node;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.Purview;

import java.util.List;

/**
 * Binary node.
 */
public class BNode extends AbstractNode {

	private int value;

	private Hub leftChildHub;
	private Hub rightChildHub;

	private final Arrow leftArrow = new BNodeLeftArrow();
	private final Arrow rightArrow = new BNodeRightArrow();
	private final List<Arrow> arrows = List.of(leftArrow, rightArrow);

	private class BNodeLeftArrow implements Arrow {
		@Override
		public ArrowDirection getDirection() {
			return ArrowDirection.LEFT;
		}

		@Override
		public Node getOrigin() {
			return BNode.this;
		}

		public Node getTarget(Purview purview) {
			net.fireGetTargetCall(getOrigin(), getDirection(), purview);
			return doGetTarget();
		}

		public void setTarget(Node target, Purview purview) {
			net.fireSetTargetCall(getOrigin(), getDirection(), target, purview);
			doSetTarget(target);
		}

		private Node doGetTarget() {
			return leftChildHub.getNode();
		}

		private void doSetTarget(Node target) {
			leftChildHub.removeInverseReference(leftArrow);
			leftChildHub = target.getHub();
			leftChildHub.addInverseReference(leftArrow);
		}
	}

	private class BNodeRightArrow implements Arrow {
		@Override
		public ArrowDirection getDirection() {
			return ArrowDirection.RIGHT;
		}

		@Override
		public Node getOrigin() {
			return BNode.this;
		}

		public Node getTarget(Purview purview) {
			net.fireGetTargetCall(getOrigin(), getDirection(), purview);
			return doGetTarget();
		}

		public void setTarget(Node target, Purview purview) {
			net.fireSetTargetCall(getOrigin(), getDirection(), target, purview);
			doSetTarget(target);
		}

		private Node doGetTarget() {
			return rightChildHub.getNode();
		}

		private void doSetTarget(Node target) {
			rightChildHub.removeInverseReference(rightArrow);
			rightChildHub = target.getHub();
			rightChildHub.addInverseReference(rightArrow);
		}
	}

	public BNode(Net net, int value, Node leftChild, Node rightChild) {
		super(net);
		this.value = value;
		initialize(leftChild, rightChild);
	}

	public BNode(Net net, Node leftChild, Node rightChild) {
		this(net, 0, leftChild, rightChild);
	}

	public BNode(Net net, Node leftChild) {
		super(net);
		initialize(leftChild, this);
	}

	public BNode(Net net) {
		super(net);
		initialize(this, this);
	}

	private void initialize(Node leftChild, Node rightChild) {
		this.leftChildHub = leftChild.getHub();
		this.leftChildHub.addInverseReference(leftArrow);
		this.rightChildHub = rightChild.getHub();
		this.rightChildHub.addInverseReference(rightArrow);
	}

	@Override
	public Node getLeftChild(Purview purview) {
		return leftArrow.getTarget(purview);
	}

	@Override
	public void setLeftChild(Node child, Purview purview) {
		leftArrow.setTarget(child, purview);
	}

	@Override
	public Node getRightChild(Purview purview) {
		return rightArrow.getTarget(purview);
	}

	@Override
	public void setRightChild(Node child, Purview purview) {
		rightArrow.setTarget(child, purview);
	}

	@Override
	public Arrow getLeftArrow() {
		return leftArrow;
	}

	@Override
	public Arrow getRightArrow() {
		return rightArrow;
	}

	@Override
	public List<Arrow> getArrows() {
		return arrows;
	}

	@Override
	public Arrow getArrow(ArrowDirection arrowDirection) {
		return switch (arrowDirection) {
			case LEFT -> leftArrow;
			case RIGHT -> rightArrow;
		};
	}

	@Override
	public int getValue() {
		return value;
	}

	@Override
	public void setValue(int value) {
		this.value = value;
	}

	@Override
	public boolean isUnaryNode() {
		return rightChildHub.getNode() == this && leftChildHub.getNode() != this;
	}

	@Override
	public boolean isCommandNode() {
		return leftChildHub.getNode() == this && rightChildHub.getNode() == this;
	}

	@Override
	public boolean isMarkerNode() {
		return false;
	}

	@Override
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
}
