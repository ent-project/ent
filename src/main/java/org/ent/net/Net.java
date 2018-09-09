package org.ent.net;

import java.util.Set;

import org.ent.net.node.CNode;
import org.ent.net.node.Hub;
import org.ent.net.node.Node;

public class Net {

	private Set<CNode> commandNodes;
	private Set<Node> internalNodes;
	private Node root;
	private NetController nc;

	public void addInternalNode(Node node) {
		internalNodes.add(node);
	}

	public void consistencyTest() {
		if (!this.contains(root)) {
			throw new AssertionError();
		}

		for (Node node : internalNodes) {
			for (Arrow arrow : node.getArrows()) {
				Node child = arrow.getTarget(nc);
				if (!this.contains(child)) {
					throw new AssertionError();
				}

				Hub childHub = child.getHub();
				if (!childHub.getInverseReferences().contains(arrow)) {
					throw new AssertionError();
				}
			}

			Hub hub = node.getHub();
			for (Arrow arrow : hub.getInverseReferences()) {
				Node childOfParent = arrow.getTarget(nc);
				if (childOfParent != node) {
					throw new AssertionError();
				}
			}
		}
	}

	private boolean contains(Node node) {
		if (node.isInternal()) {
			return internalNodes.contains(node);
		} else {
			return commandNodes.contains(node);
		}
	}

}
