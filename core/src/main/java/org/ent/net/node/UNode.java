package org.ent.net.node;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Manner;
import org.ent.net.Net;

/**
 * Unary node.
 *
 * (Node with a single child, used like a pointer.)
 */
public class UNode extends Node {

	private Hub childHub;

	private final Arrow arrow = new UNodeArrow();

	private final List<Arrow> arrows = Collections.singletonList(arrow);

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
		public Node getTarget(Manner manner) {
			net.fireGetTargetCall(getOrigin(), getDirection(), manner);
			return doGetTarget();
		}

		@Override
		public void setTarget(Node target, Manner manner) {
			net.fireSetTargetCall(getOrigin(), getDirection(), target, manner);
			doSetTarget(target);
		}

		private Node doGetTarget() {
			return childHub.getNode();
		}

		private void doSetTarget(Node target) {
			childHub.removeInverseReference(arrow);
			childHub = target.getHub();
			childHub.addInverseReference(arrow);
		}
	}

	public UNode(Net net, Node child) {
		super(net);
		initialize(child);
	}

	public UNode(Net net) {
		super(net);
		initialize(this);
	}

	private void initialize(Node child) {
		this.childHub = child.getHub();
		this.childHub.addInverseReference(arrow);
	}

	public Node getChild(Manner manner) {
		return arrow.getTarget(manner);
	}

	public void setChild(Node child, Manner manner) {
		arrow.setTarget(child, manner);
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
