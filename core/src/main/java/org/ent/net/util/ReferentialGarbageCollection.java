package org.ent.net.util;

import java.util.Set;

import org.ent.net.Arrow;
import org.ent.net.DefaultNetController;
import org.ent.net.Net;
import org.ent.net.NetController;
import org.ent.net.node.MarkerNode;
import org.ent.net.node.Node;

public class ReferentialGarbageCollection {

	private final Net net;

	private final NetController controller;

	public ReferentialGarbageCollection(Net net) {
		this.net = net;
		this.controller = new DefaultNetController(net);
	}

	public void run() {
		Set<Node> reachableNodes = NetUtils.collectReachable(net.getRoot());
		for (Node n : net.getNodes()) {
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
			controller.setTarget(arrow, n);
		}
	}
}
