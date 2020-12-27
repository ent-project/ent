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

	private Hub childHub;

	private final Arrow arrow;

	private final List<Arrow> arrows;

	private class UNodeArrow implements Arrow {
		@Override
		public ArrowDirection getDirection() {
			return ArrowDirection.DOWN;
		}

		@Override
		public Node getOrigin() {
			return UNode.this;
		}

		@Override
		public Node getTargetForNetControllerOnly() {
			return childHub.getNode();
		}

		@Override
		public void setTargetForNetControllerOnly(Node target) {
			childHub.removeInverseReference(arrow);
			childHub = target.getHub();
			childHub.addInverseReference(arrow);
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
		return switch (arrowDirection) {
			case DOWN -> arrow;
			case LEFT, RIGHT -> throw new IllegalArgumentException();
		};
	}

	@Override
	public Optional<Arrow> getArrowMaybe(ArrowDirection arrowDirection) {
		return switch (arrowDirection) {
			case DOWN -> Optional.of(arrow);
			case LEFT, RIGHT -> Optional.empty();
		};
	}

	@Override
	public boolean isInternal() {
		return true;
	}

}
