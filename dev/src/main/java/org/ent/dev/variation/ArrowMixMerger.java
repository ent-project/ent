package org.ent.dev.variation;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.util.ModifiedPoisson;

import java.util.Random;
import java.util.Set;

public class ArrowMixMerger {

    private final double frequencyFactor;

    private final Random rand;
    private final Net netPrimary;
    private final Net netJoining;

    public ArrowMixMerger(Net netPrimary, Net netJoining, Random random, double frequencyFactor) {
        this.netPrimary = netPrimary;
        this.netJoining = netJoining;
        this.rand = random;
        this.frequencyFactor = frequencyFactor;
    }

    public void execute() {
        Set<Node> nodes = netJoining.removeAllNodes();
        netPrimary.addNodes(nodes);
        int num = (int) (netPrimary.getNodes().size() * frequencyFactor);
        int noMutations = ModifiedPoisson.getModifiedPoisson(num).drawModifiedPoisson(rand);
        for (int i = 0; i < noMutations; i++) {
            rewireOneArrowRandomly();
        }
    }

    private void rewireOneArrowRandomly() {
        int i = rand.nextInt(netPrimary.getNodes().size());
        Node node = netPrimary.getNodesAsList().get(i);
        Arrow arrow = node.getArrow(rand.nextBoolean() ? ArrowDirection.LEFT : ArrowDirection.RIGHT);
        int indexTarget = rand.nextInt(netPrimary.getNodes().size());
        Node nodeTarget = netPrimary.getNodesAsList().get(indexTarget);
        arrow.setTarget(nodeTarget, Purview.DIRECT);
    }
}
