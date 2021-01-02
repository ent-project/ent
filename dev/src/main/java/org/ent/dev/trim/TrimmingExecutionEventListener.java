package org.ent.dev.trim;

import java.util.HashSet;
import java.util.Set;

import org.ent.ExecutionEventListener;
import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Manner;
import org.ent.net.node.Node;

class TrimmingExecutionEventListener implements ExecutionEventListener {

	private final Set<Arrow> requiredArrows;

	private final Set<Arrow> overriddenArrows;

	public TrimmingExecutionEventListener() {
		this.requiredArrows = new HashSet<>();
		this.overriddenArrows = new HashSet<>();
	}

	@Override
	public void fireGetChild(Node n, ArrowDirection arrowDirection, Manner manner) {
		if (isApplicableManner(manner)) {
			Arrow arrow = n.getArrow(arrowDirection);
			if (!overriddenArrows.contains(arrow)) {
				requiredArrows.add(arrow);
			}
		}
	}

	private boolean isApplicableManner(Manner manner) {
		return manner == Manner.RUNNER || manner == Manner.COMMAND;
	}

	@Override
	public void fireSetChild(Node from, ArrowDirection arrowDirection, Node to, Manner manner) {
		if (isApplicableManner(manner)) {
			Arrow arrow = from.getArrow(arrowDirection);
			overriddenArrows.add(arrow);
		}
	}

	@Override
	public void fireNewNode(Node n) {
	}

	public boolean isDead(Arrow arrow) {
		return !requiredArrows.contains(arrow);
	}
}