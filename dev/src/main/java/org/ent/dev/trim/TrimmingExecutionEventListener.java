package org.ent.dev.trim;

import java.util.HashSet;
import java.util.Set;

import org.ent.ExecutionEventListener;
import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.ExecutionContext;
import org.ent.net.node.Node;

class TrimmingExecutionEventListener implements ExecutionEventListener {

	private final Set<Arrow> requiredArrows;

	private final Set<Arrow> overriddenArrows;

	public TrimmingExecutionEventListener() {
		this.requiredArrows = new HashSet<>();
		this.overriddenArrows = new HashSet<>();
	}

	@Override
	public void fireExecutionStart() {
	}

	@Override
	public void fireGetChild(Node n, ArrowDirection arrowDirection, ExecutionContext context) {
		Arrow arrow = n.getArrow(arrowDirection);
		if (!overriddenArrows.contains(arrow)) {
			requiredArrows.add(arrow);
		}
	}

	@Override
	public void fireSetChild(Node from, ArrowDirection arrowDirection, Node to, ExecutionContext context) {
		Arrow arrow = from.getArrow(arrowDirection);
		overriddenArrows.add(arrow);
	}

	@Override
	public void fireNewNode(Node n) {
	}

	public boolean isDead(Arrow arrow) {
		return !requiredArrows.contains(arrow);
	}
}