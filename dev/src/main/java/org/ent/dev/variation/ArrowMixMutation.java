package org.ent.dev.variation;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.util.ModifiedPoisson;

public class ArrowMixMutation {

    private final double frequencyFactor;

    private final Net net;

    private final UniformRandomProvider rand;

    public ArrowMixMutation(double frequencyFactor, Net net, UniformRandomProvider rand) {
        this.frequencyFactor = frequencyFactor;
        this.net = net;
        this.rand = rand;
    }

    public void execute() {
        int num = (int) (net.getNodes().size() * frequencyFactor);
        int noMutations = ModifiedPoisson.getModifiedPoisson(num).drawModifiedPoisson(rand);
        for (int i = 0; i < noMutations; i++) {
            rewireOneArrowRandomly();
        }
    }

    private void rewireOneArrowRandomly() {
        int i = rand.nextInt(net.getNodes().size());
        Node node = net.getNodesAsList().get(i);
        Arrow arrow = node.getArrow(rand.nextBoolean() ? ArrowDirection.LEFT : ArrowDirection.RIGHT);
        int indexTarget = rand.nextInt(net.getNodes().size());
        Node nodeTarget = net.getNodesAsList().get(indexTarget);
        arrow.setTarget(nodeTarget, Purview.DIRECT);
    }
}
