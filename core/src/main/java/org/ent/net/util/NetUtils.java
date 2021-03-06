package org.ent.net.util;

import java.util.LinkedHashSet;
import java.util.Set;

import org.ent.net.Arrow;
import org.ent.net.Manner;
import org.ent.net.node.Node;

public final class NetUtils {

    private NetUtils() {
    }

    public static Set<Node> collectReachable(Node root) {
        Set<Node> reachableNodes = new LinkedHashSet<>();
        doCollectReachableRec(root, reachableNodes);
        return reachableNodes;
    }

    private static void doCollectReachableRec(Node node, Set<Node> reachableNodes) {
        if (reachableNodes.contains(node))
            return;
        reachableNodes.add(node);
    	for (Arrow arrow : node.getArrows()) {
    		Node child = arrow.getTarget(Manner.DIRECT);
    		doCollectReachableRec(child, reachableNodes);
    	}
    }

}
