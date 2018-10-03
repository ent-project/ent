package org.ent.net.node;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.NetController;

/**
 * Unary node.
 *
 * (Node with a single child, used like a pointer.)
 */
public class UNode extends Node {

	public Hub childHub;

	private final Arrow arrow;

	private final List<Arrow> arrows;

	private class UNodeArrow implements Arrow {
		@Override
		public ArrowDirection getType() {
			return ArrowDirection.DOWN;
		}

		@Override
		public Node getOrigin() {
			return UNode.this;
		}

		@Override
		public Node getTargetForNetControllerOnly() {
			return UNode.this.getChildForController();
		}

		@Override
		public void setTargetForNetControllerOnly(Node target) {
			UNode.this.setChildForController(target);
		}
	}

	public UNode(Node child) {
		super();
		this.arrow = new UNodeArrow();
		this.childHub = child.getHub();
		this.childHub.addInverseReference(arrow);
		this.arrows = Collections.singletonList(arrow);
	}

	public Node getChild(NetController controller) {
		return controller.getTarget(arrow);
	}

	public void setChild(NetController controller, Node child) {
		controller.setTarget(arrow, child);
	}

	public Arrow getArrow() {
		return arrow;
	}

	@Override
	public List<Arrow> getArrows() {
		return arrows;
	}

	@Override
	public Arrow getArrow(ArrowDirection arrowDirection) {
		switch (arrowDirection) {
		case DOWN:
			return arrow;
		case LEFT:
		case RIGHT:
			throw new IllegalArgumentException();
		default:
			throw new AssertionError();
		}
	}

	@Override
	public Optional<Arrow> getArrowMaybe(ArrowDirection arrowDirection) {
		switch (arrowDirection) {
		case DOWN:
			return Optional.of(arrow);
		case LEFT:
		case RIGHT:
			return Optional.empty();
		default:
			throw new AssertionError();
		}
	}

	@Override
	public boolean isInternal() {
		return true;
	}

	private Node getChildForController() {
		return childHub.getNode();
	}

	private void setChildForController(Node node) {
		this.childHub.removeInverseReference(arrow);
		this.childHub = node.getHub();
		this.childHub.addInverseReference(arrow);
	}

}
