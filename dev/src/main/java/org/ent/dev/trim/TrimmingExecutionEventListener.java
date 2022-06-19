package org.ent.dev.trim;

import org.ent.ExecutionEventListener;
import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Purview;
import org.ent.net.node.Node;

import java.util.HashSet;
import java.util.Set;

class TrimmingExecutionEventListener implements ExecutionEventListener {

	private final Set<Arrow> requiredArrows;

	private final Set<Arrow> overriddenArrows;

	public TrimmingExecutionEventListener() {
		this.requiredArrows = new HashSet<>();
		this.overriddenArrows = new HashSet<>();
	}

	@Override
	public void calledGetChild(Node n, ArrowDirection arrowDirection, Purview purview) {
		if (isApplicablePurview(purview)) {
			Arrow arrow = n.getArrow(arrowDirection);
			if (!overriddenArrows.contains(arrow)) {
				requiredArrows.add(arrow);
			}
		}
	}

	private boolean isApplicablePurview(Purview purview) {
		return purview == Purview.RUNNER || purview == Purview.COMMAND;
	}

	@Override
	public void calledSetChild(Node from, ArrowDirection arrowDirection, Node to, Purview purview) {
		if (isApplicablePurview(purview)) {
			Arrow arrow = from.getArrow(arrowDirection);
			overriddenArrows.add(arrow);
		}
	}

	@Override
	public void calledNewNode(Node n) {
	}

	public boolean isDead(Arrow arrow) {
		return !requiredArrows.contains(arrow);
	}
}