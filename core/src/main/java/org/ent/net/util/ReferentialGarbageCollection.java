package org.ent.net.util;

import org.ent.permission.Permissions;
import org.ent.net.Arrow;
import org.ent.net.Net;
import org.ent.net.node.MarkerNode;
import org.ent.net.node.Node;

import java.util.Set;

public class ReferentialGarbageCollection {

	private final Net net;

	public ReferentialGarbageCollection(Net net) {
		this.net = net;
	}

	public void run() {
		Set<Node> reachableNodes = NetUtils.collectReachable(net.getRoot());
		for (Node n : net.getNodes()) {
			if (n == null) {
				continue;
			}
			if (!reachableNodes.contains(n)) {
				unlink(n);
			}
		}
		net.removeNodeIf(node -> !reachableNodes.contains(node));
	}

	private void unlink(Node n) {
		if (n instanceof MarkerNode) {
			return;
		}
		for (Arrow arrow : n.getArrows()) {
			arrow.setTarget(n, Permissions.DIRECT);
		}
	}
}
