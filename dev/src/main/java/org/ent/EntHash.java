package org.ent;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;

import java.util.List;

public class EntHash {
    private static final int MURMUR_SEED = 0x234f3a;
    private static final HashFunction MURMUR_3 = Hashing.murmur3_32_fixed(MURMUR_SEED);

    public static int hash(Ent ent) {
        Hasher hasher = MURMUR_3.newHasher();

        Net net = ent.getNet();

        hasher.putInt(net.getRoot().getIndex());

        List<Node> nodes = net.getNodesAsList();
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (node != null) {
                hasher.putInt(i);
                hasher.putInt(node.getValue(Purview.DIRECT));
                hasher.putInt(node.getLeftChild(Purview.DIRECT).getIndex());
                hasher.putInt(node.getRightChild(Purview.DIRECT).getIndex());
            }
        }
        return hasher.hash().asInt();
    }
}
