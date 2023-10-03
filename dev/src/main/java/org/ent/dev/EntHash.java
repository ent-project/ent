package org.ent.dev;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.ent.Ent;
import org.ent.permission.Permissions;
import org.ent.net.Net;
import org.ent.net.node.Node;

import java.util.List;

public class EntHash {
    private static final int MURMUR_SEED = 0x234f3a;
    private static final HashFunction MURMUR_3 = Hashing.murmur3_32_fixed(MURMUR_SEED);
    public static final int DOMAIN_SEPARATOR = 0xfe110e57;

    // xor with net index to reduce chances it gets mixed up with other types of indices
    // that happen to have the same value
    public static final int NET_INDEX_MODIFIER = 0b1011011010010100100100010100110;

    public static int hash(Ent ent) {
        Hasher hasher = MURMUR_3.newHasher();
        doHash(ent.getNet(), hasher);
        for (int i = 0; i < ent.getDomains().size(); i++) {
            hasher.putInt(DOMAIN_SEPARATOR);
            hasher.putInt(i);
            doHash(ent.getDomains().get(i), hasher);
        }
        return hasher.hash().asInt();
    }

    public static int hash(Net net) {
        Hasher hasher = MURMUR_3.newHasher();
        doHash(net, hasher);
        return hasher.hash().asInt();
    }

    private static void doHash(Net net, Hasher hasher) {
        hasher.putInt(net.getRoot().getIndex());

        List<Node> nodes = net.getNodesAsList();
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (node != null) {
                hasher.putInt(i);
                hasher.putInt(node.getValue(Permissions.DIRECT));
                Node leftChild = node.getLeftChild(Permissions.DIRECT);
                hasher.putInt(leftChild.getIndex());
                if (leftChild.getNet() != net) {
                    hasher.putInt(leftChild.getNet().getNetIndex() ^ NET_INDEX_MODIFIER);
                }
                Node rightChild = node.getRightChild(Permissions.DIRECT);
                hasher.putInt(rightChild.getIndex());
                if (rightChild.getNet() != net) {
                    hasher.putInt(rightChild.getNet().getNetIndex() ^ NET_INDEX_MODIFIER);
                }
            }
        }
    }
}
