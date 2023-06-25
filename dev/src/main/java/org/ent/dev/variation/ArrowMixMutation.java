package org.ent.dev.variation;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.util.ModifiedPoisson;

import java.util.Random;

public class ArrowMixMutation {

    public static final int FREQUENCY_FACTOR = 1;

    private final Net net;

    private final Random rand;


    public ArrowMixMutation(Net net, Random rand) {
        this.net = net;
        this.rand = rand;
    }

    public void execute() {
        int num = net.getNodes().size() * FREQUENCY_FACTOR;
        int noMutations = ModifiedPoisson.getModifiedPoisson(num).drawModifiedPoisson(rand);
        for (int i = 0; i < noMutations; i++) {
            int i1 = rand.nextInt(net.getNodes().size());
            Node node1 = net.getNodesAsList().get(i1);
            Arrow arrow = node1.getArrow(rand.nextBoolean() ? ArrowDirection.LEFT : ArrowDirection.RIGHT);
            int indexTarget = rand.nextInt(net.getNodes().size());
            Node nodeTarget = net.getNodesAsList().get(indexTarget);
            arrow.setTarget(nodeTarget, Purview.DIRECT);
        }
    }
}
