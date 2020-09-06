package org.ent.net;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

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

	public boolean addNode(Node node) {
		return nodes.add(node);
	}

	public boolean addNodes(Collection<Node> nodes) {
		return this.nodes.addAll(nodes);
	}

	public boolean removeNode(Node node) {
		return nodes.remove(node);
	}

	public boolean removeNodeIf(Predicate<? super Node> filter) {
		return nodes.removeIf(filter);
	}

	public void clearNodes() {
		nodes.clear();
	}

	public Node getRoot() {
		return root;
	}

	public void setRoot(Node root) {
		this.root = root;
	}

	public boolean belongsToNet(Node node) {
		if (node == null) {
			throw new IllegalArgumentException("node must not be null");
		}
		return node == markerNode || nodes.contains(node);
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
			consistencyTest(node);
		}
	}

	private void consistencyTest(Node node) {
		if (node instanceof MarkerNode) {
			throw new AssertionError("Net node must not be a marker node");
		}
		for (Arrow arrow : node.getArrows()) {
			consistencyTest(arrow);
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

	private void consistencyTest(Arrow arrow) {
		Node child = arrow.getTarget(internalNetController);
		if (child instanceof MarkerNode) {
			if (!markerNodePermitted) {
				throw new AssertionError("Child of node is marker node, but they are not permitted");
			} else if (child != markerNode) {
				throw new AssertionError("Child of node is marker node, but not the designated one");
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

	public void referentialGarbageCollection() {
		new ReferentialGarbageCollection(this).run();
	}

	public void runWithMarkerNode(Consumer<MarkerNode> consumer) {
		MarkerNode marker = new MarkerNode();
		permitMarkerNode(marker);
		consumer.accept(marker);
		forbidMarkerNode();
	}

}
