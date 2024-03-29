package org.ent.dev.variation;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.permission.Permissions;
import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.node.Node;
import org.ent.util.ModifiedPoisson;

public class ArrowMixMutation {

    private final double frequencyFactor;

    private final Net net;

    private final UniformRandomProvider rand;

    private Range sourceRange, destinationRange;

    public record Range(int minIncluive, int maxExclusive) {
        public int size() {
            return maxExclusive - minIncluive;
        }
    }

    public ArrowMixMutation(double frequencyFactor, Net net, UniformRandomProvider rand) {
        this.frequencyFactor = frequencyFactor;
        this.net = net;
        this.rand = rand;
        this.sourceRange = this.destinationRange = new Range(0, net.getNodes().size());
    }

    public void setSourceRange(int minIncluive, int maxExclusive) {
        this.sourceRange = new Range(minIncluive, maxExclusive);
    }

    public void setDestinationRange(int minIncluive, int maxExclusive) {
        this.destinationRange = new Range(minIncluive, maxExclusive);
    }

    protected int mapTargetIndex(int index) {
        return index;
    }

    protected Node resolveTargetNode(int indexTargetResolved) {
        return net.getNodesAsList().get(indexTargetResolved);
    }

    public void execute() {
        int num = (int) (sourceRange.size() * frequencyFactor);
        int noMutations = ModifiedPoisson.getModifiedPoisson(num).drawModifiedPoisson(rand);
        for (int i = 0; i < noMutations; i++) {
            rewireOneArrowRandomly();
        }
    }

    private void rewireOneArrowRandomly() {
        int indexSource = rand.nextInt(sourceRange.minIncluive, sourceRange.maxExclusive);
        Node node = net.getNodesAsList().get(indexSource);
        Arrow arrow = node.getArrow(drawArrowDirection());
        int indexTarget = rand.nextInt(destinationRange.minIncluive, destinationRange.maxExclusive);
        int indexTargetResolved = mapTargetIndex(indexTarget);
        Node nodeTarget = resolveTargetNode(indexTargetResolved);
        arrow.setTarget(nodeTarget, Permissions.DIRECT);
    }

    protected ArrowDirection drawArrowDirection() {
        return rand.nextBoolean() ? ArrowDirection.LEFT : ArrowDirection.RIGHT;
    }
}
