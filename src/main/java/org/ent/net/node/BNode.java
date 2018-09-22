package org.ent.net.node;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.NetController;

/**
 * Binary node.
 */
public class BNode extends Node {

	private Hub leftChildHub;
	private Hub rightChildHub;

	private final Arrow leftArrow;
	private final Arrow rightArrow;
	private final List<Arrow> arrows;

	private class BNodeLeftArrow implements Arrow {
		@Override
		public ArrowDirection getType() {
			return ArrowDirection.LEFT;
		}

		@Override
		public Node getOrigin() {
			return BNode.this;
		}

		@Override
		public Node getTargetForNetControllerOnly() {
			return BNode.this.getLeftChildForController();
		}

		@Override
		public void setTargetForNetControllerOnly(Node target) {
			BNode.this.setLeftChildForController(target);
		}
	}

	private class BNodeRightArrow implements Arrow {
		@Override
		public ArrowDirection getType() {
			return ArrowDirection.RIGHT;
		}

		@Override
		public Node getOrigin() {
			return BNode.this;
		}

		@Override
		public Node getTargetForNetControllerOnly() {
			return BNode.this.getRightChildForController();
		}

		@Override
		public void setTargetForNetControllerOnly(Node target) {
			BNode.this.setRightChildForController(target);
		}
	}

	public BNode(Node leftChild, Node rightChild) {
		super();
		this.leftArrow = new BNodeLeftArrow();
		this.rightArrow = new BNodeRightArrow();
		this.arrows = Arrays.asList(leftArrow, rightArrow);
		this.leftChildHub = leftChild.getHub();
		this.leftChildHub.addInverseReference(leftArrow);
		this.rightChildHub = rightChild.getHub();
		this.rightChildHub.addInverseReference(rightArrow);
	}

	public Node getLeftChild(NetController nc) {
		return nc.getTarget(leftArrow);
	}

	public void setLeftChild(NetController nc, Node child) {
		nc.setTarget(leftArrow, child);
	}

	public Node getRightChild(NetController nc) {
		return nc.getTarget(rightArrow);
	}

	public void setRightChild(NetController nc, Node child) {
		nc.setTarget(rightArrow, child);
	}

	public Arrow getLeftArrow() {
		return leftArrow;
	}

	public Arrow getRightArrow() {
		return rightArrow;
	}

	@Override
	public List<Arrow> getArrows() {
		return arrows;
	}

	@Override
	public Arrow getArrow(ArrowDirection arrowDirection) {
		switch (arrowDirection) {
		case LEFT:
			return leftArrow;
		case RIGHT:
			return rightArrow;
		case DOWN:
			throw new IllegalArgumentException();
		default:
			throw new AssertionError();
		}
	}

	@Override
	public Optional<Arrow> getArrowMaybe(ArrowDirection arrowDirection) {
		switch (arrowDirection) {
		case LEFT:
			return Optional.of(leftArrow);
		case RIGHT:
			return Optional.of(rightArrow);
		case DOWN:
			return Optional.empty();
		default:
			throw new AssertionError();
		}
	}

	@Override
	public boolean isInternal() {
		return true;
	}

	private Node getLeftChildForController() {
		return leftChildHub.getNode();
	}

	private Node getRightChildForController() {
		return rightChildHub.getNode();
	}

	private void setLeftChildForController(Node n) {
		this.leftChildHub.removeInverseReference(leftArrow);
		this.leftChildHub = n.getHub();
		this.leftChildHub.addInverseReference(leftArrow);
	}

	private void setRightChildForController(Node n) {
		this.rightChildHub.removeInverseReference(rightArrow);
		this.rightChildHub = n.getHub();
		this.rightChildHub.addInverseReference(rightArrow);
	}

}
