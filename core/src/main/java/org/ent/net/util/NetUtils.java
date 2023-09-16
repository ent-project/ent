package org.ent.net.util;

import org.ent.net.Arrow;
import org.ent.net.node.Node;
import org.ent.permission.Permissions;

import java.util.LinkedHashSet;
import java.util.Set;

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
    		Node child = arrow.getTarget(Permissions.DIRECT);
    		doCollectReachableRec(child, reachableNodes);
    	}
    }

}
