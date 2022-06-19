package org.ent.net.node;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Purview;
import org.ent.net.Net;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Binary node.
 */
public class BNode extends Node {

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

	public BNode(Net net, Node leftChild, Node rightChild) {
		super(net);
		initialize(leftChild, rightChild);
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
		};
	}

	@Override
	public Optional<Arrow> getArrowMaybe(ArrowDirection arrowDirection) {
		return switch (arrowDirection) {
			case LEFT -> Optional.of(leftArrow);
			case RIGHT -> Optional.of(rightArrow);
			case DOWN -> Optional.empty();
		};
	}

	@Override
	public <T> T instanceOf(Function<CNode, T> cNodeCase, Function<UNode, T> uNodeCase, Function<BNode, T> bNodeCase) {
		return bNodeCase.apply(this);
	}

}
