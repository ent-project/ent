package org.ent.dev.variation;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.permission.Permissions;
import org.ent.net.Net;
import org.ent.net.node.Node;
import org.ent.net.util.RandomUtil;
import org.ent.util.ModifiedPoisson;

public class ValueFragmentCrossover {
    public static final int[] MASKS = new int[] {0xFFF, 0xF000, 0xF_0000, 0xF0_0000, 0xFF00_0000};

    private final double frequencyFactor;

    private final UniformRandomProvider rand;
    private final Net net1;
    private final Net net2;

    public ValueFragmentCrossover(Net net1, Net net2, long seed, double frequencyFactor) {
        this.net1 = net1;
        this.net2 = net2;
        this.rand = RandomUtil.newRandom2(seed);
        this.frequencyFactor = frequencyFactor;
    }

    public void execute() {
        int num = (int) (net1.getNodes().size() * frequencyFactor);
        int noNodes = ModifiedPoisson.getModifiedPoisson(num).drawModifiedPoisson(rand);
        for (int i = 0; i < noNodes; i++) {
            int i1 = rand.nextInt(net1.getNodes().size());
            Node node1 = net1.getNodesAsList().get(i1);
            int i2 = rand.nextInt(net2.getNodes().size());
            Node node2 = net2.getNodesAsList().get(i2);
            int sel = rand.nextInt(MASKS.length);
            swapValueFragment(node1, node2, sel);
        }
    }

    static void swapValueFragment(Node node1, Node node2, int maskIndex) {
        int mask = MASKS[maskIndex];
        int value1 = node1.getValue(Permissions.DIRECT);
        int value2 = node2.getValue(Permissions.DIRECT);
        node1.setValue((value1 & (~mask)) | (value2 & mask));
        node2.setValue((value2 & (~mask)) | (value1 & mask));
    }
}
