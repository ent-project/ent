package org.ent.dev.trim;

import java.util.HashSet;
import java.util.Set;

import org.ent.ExecutionEventListener;
import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.node.CNode;
import org.ent.net.node.Node;

class TrimmingExecutionEventListener implements ExecutionEventListener {

	private final Set<Arrow> requiredArrows;

	private final Set<Arrow> overrriddenArrows;

	public TrimmingExecutionEventListener() {
		this.requiredArrows = new HashSet<>();
		this.overrriddenArrows = new HashSet<>();
	}

	@Override
	public void fireExecutionStart() {
	}

	@Override
	public void fireGetChild(Node n, ArrowDirection arrowDirection) {
		Arrow arrow = n.getArrow(arrowDirection);
		if (!overrriddenArrows.contains(arrow)) {
			requiredArrows.add(arrow);
		}
	}

	@Override
	public void fireSetChild(Node from, ArrowDirection arrowDirection, Node to) {
		Arrow arrow = from.getArrow(arrowDirection);
		overrriddenArrows.add(arrow);
	}

	@Override
	public void fireNewNode(Node n) {
	}

	@Override
	public void fireCommandExecuted(CNode cmd) {
	}

	public boolean isDead(Arrow arrow) {
		return !requiredArrows.contains(arrow);
	}
}