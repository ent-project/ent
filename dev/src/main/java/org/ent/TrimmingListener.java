package org.ent;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Purview;
import org.ent.net.node.Node;

import java.util.HashSet;
import java.util.Set;

public class TrimmingListener  extends NopNetEventListener {

    private final Set<Integer> requiredArrows = new HashSet<>();

    private final Set<Integer> overriddenArrows = new HashSet<>();

    @Override
    public void calledGetChild(Node n, ArrowDirection arrowDirection, Purview purview) {
        if (isApplicablePurview(purview)) {
            Arrow arrow = n.getArrow(arrowDirection);
            int index = arrow.getIndex();
            if (!overriddenArrows.contains(index)) {
                requiredArrows.add(index);
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
            overriddenArrows.add(arrow.getIndex());
        }
    }

    public boolean isDead(Arrow arrow) {
        return !requiredArrows.contains(arrow.getIndex());
    }
}
