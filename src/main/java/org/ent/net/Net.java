package org.ent.net;

import java.util.HashSet;
import java.util.Set;

import org.ent.net.node.Hub;
import org.ent.net.node.Node;

public class Net {

	private Set<Node> nodes;

	private Node root;
	private NetController internalNetController;

	public Net() {
		this.nodes = new HashSet<>();
		this.internalNetController = new ReadOnlyNetController();
	}

	public Set<Node> getNodes() {
		return nodes;
	}

	public void addNode(Node node) {
		nodes.add(node);
	}

	public Node getRoot() {
		return root;
	}

	public void setRoot(Node root) {
		this.root = root;
	}

	public void consistencyTest() {
		if (root == null) {
			throw new AssertionError();
		}
		if (!this.nodes.contains(root)) {
			throw new AssertionError();
		}

		for (Node node : nodes) {
			for (Arrow arrow : node.getArrows()) {
				Node child = arrow.getTarget(internalNetController);
				if (!this.nodes.contains(child)) {
					throw new AssertionError();
				}

				Hub childHub = child.getHub();
				if (!childHub.getInverseReferences().contains(arrow)) {
					throw new AssertionError();
				}
			}

			Hub hub = node.getHub();
			for (Arrow arrow : hub.getInverseReferences()) {
				Node childOfParent = arrow.getTarget(internalNetController);
				if (childOfParent != node) {
					throw new AssertionError();
				}
			}
		}
	}

}
