package org.ent.net;

import java.util.LinkedHashSet;
import java.util.Set;

import org.ent.net.node.Hub;
import org.ent.net.node.MarkerNode;
import org.ent.net.node.Node;
import org.ent.net.util.ReferentialGarbageCollection;

public class Net {

	private Set<Node> nodes;

	private Node root;
	private NetController internalNetController;

	private boolean markerNodePermitted;

	private MarkerNode markerNode;

	public Net() {
		this.nodes = new LinkedHashSet<>();
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

	public void permitMarkerNode(MarkerNode markerNode) {
		this.markerNodePermitted = true;
		this.markerNode = markerNode;
	}

	public void forbidMarkerNode() {
		this.markerNodePermitted = false;
		this.markerNode = null;
	}

	public boolean isMarkerNodePermitted() {
		return markerNodePermitted;
	}

	public MarkerNode getMarkerNode() {
		return markerNode;
	}

	public void consistencyTest() {
		if (root == null) {
			throw new AssertionError("Root is null");
		}
		if (!this.nodes.contains(root)) {
			throw new AssertionError("Root must be one of the net nodes");
		}

		for (Node node : nodes) {
			for (Arrow arrow : node.getArrows()) {
				Node child = arrow.getTarget(internalNetController);
				if (child instanceof MarkerNode) {
					if (!markerNodePermitted) {
						throw new AssertionError("Child of node is MarkerNode, but they are not permitted");
					}
				} else {
					if (!nodes.contains(child)) {
						throw new AssertionError("Child of node must be part of the net");
					}

					Hub childHub = child.getHub();
					if (!childHub.getInverseReferences().contains(arrow)) {
						throw new AssertionError("Child nodes must be aware of their parents");
					}
				}
			}

			Hub hub = node.getHub();
			for (Arrow arrow : hub.getInverseReferences()) {
				if (!nodes.contains(arrow.getOrigin()))
					throw new AssertionError("Nodes referencing a net node must be part of the net");
				Node childOfParent = arrow.getTarget(internalNetController);
				if (childOfParent != node) {
					throw new AssertionError("Node must be child of its parent");
				}
			}
		}
	}

	public void referentialGarbageCollection() {
		new ReferentialGarbageCollection(this).run();
	}

}
