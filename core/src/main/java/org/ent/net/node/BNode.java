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
		public ArrowDirection getDirection() {
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
		public ArrowDirection getDirection() {
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

	public Node getLeftChild(NetController controller) {
		return controller.getTarget(leftArrow);
	}

	public void setLeftChild(NetController controller, Node child) {
		controller.setTarget(leftArrow, child);
	}

	public Node getRightChild(NetController controller) {
		return controller.getTarget(rightArrow);
	}

	public void setRightChild(NetController controller, Node child) {
		controller.setTarget(rightArrow, child);
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
		return switch (arrowDirection) {
			case LEFT -> leftArrow;
			case RIGHT -> rightArrow;
			case DOWN -> throw new IllegalArgumentException();
			default -> throw new AssertionError();
		};
	}

	@Override
	public Optional<Arrow> getArrowMaybe(ArrowDirection arrowDirection) {
		return switch (arrowDirection) {
			case LEFT -> Optional.of(leftArrow);
			case RIGHT -> Optional.of(rightArrow);
			case DOWN -> Optional.empty();
			default -> throw new AssertionError();
		};
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

	private void setLeftChildForController(Node child) {
		this.leftChildHub.removeInverseReference(leftArrow);
		this.leftChildHub = child.getHub();
		this.leftChildHub.addInverseReference(leftArrow);
	}

	private void setRightChildForController(Node child) {
		this.rightChildHub.removeInverseReference(rightArrow);
		this.rightChildHub = child.getHub();
		this.rightChildHub.addInverseReference(rightArrow);
	}

}
