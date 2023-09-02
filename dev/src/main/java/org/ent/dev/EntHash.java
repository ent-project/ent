package org.ent.dev;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.ent.Ent;
import org.ent.LazyPortalArrow;
import org.ent.net.Arrow;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;

import java.util.List;

public class EntHash {
    private static final int MURMUR_SEED = 0x234f3a;
    private static final HashFunction MURMUR_3 = Hashing.murmur3_32_fixed(MURMUR_SEED);
    public static final int DOMAIN_SEPARATOR = 0xfe110e57;
    public static final int PORTAL_SEPARATOR = 0xaaee83dd;


    public static int hash(Ent ent) {
        Hasher hasher = MURMUR_3.newHasher();
        doHash(ent.getNet(), hasher);
        for (int i = 0; i < ent.getDomains().size(); i++) {
            hasher.putInt(DOMAIN_SEPARATOR);
            hasher.putInt(i);
            doHash(ent.getDomains().get(i), hasher);
        }
        for (int i = 0; i < ent.getPortals().size(); i++) {
            hasher.putInt(PORTAL_SEPARATOR);
            hasher.putInt(i);
            Arrow arrow = ent.getPortals().get(i);
            Node target = null;
            if (arrow instanceof LazyPortalArrow lazyPortalArrow) {
                if (lazyPortalArrow.isInitialized()) {
                    target = lazyPortalArrow.getTarget(Purview.DIRECT);
                }
            } else {
                target = arrow.getTarget(Purview.DIRECT);
            }
            if (target != null) {
                hasher.putInt(target.getIndex());
            }
        }
        return hasher.hash().asInt();
    }

    private static int hash(Net net) {
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
                hasher.putInt(node.getValue(Purview.DIRECT));
                hasher.putInt(node.getLeftChild(Purview.DIRECT).getIndex());
                hasher.putInt(node.getRightChild(Purview.DIRECT).getIndex());
            }
        }
    }
}
