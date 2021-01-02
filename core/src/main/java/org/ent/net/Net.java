package org.ent.net;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.Validate;
import org.ent.ExecutionEventListener;
import org.ent.net.node.BNode;
import org.ent.net.node.CNode;
import org.ent.net.node.Hub;
import org.ent.net.node.MarkerNode;
import org.ent.net.node.Node;
import org.ent.net.node.UNode;
import org.ent.net.node.cmd.Command;
import org.ent.net.util.ReferentialGarbageCollection;

import javax.validation.constraints.NotNull;

public class Net {

	private static final boolean VALIDATE = true;

	private Set<Node> nodes;

	private Node root;

	private boolean markerNodePermitted;

	private MarkerNode markerNode;

	Set<ExecutionEventListener> eventListeners = new HashSet<>();

	public Net() {
		this.nodes = new LinkedHashSet<>();
	}

	public Set<Node> getNodes() {
		return nodes;
	}

	public void addNode(Node node) {
		if (node.getNet() != null) {
			throw new IllegalArgumentException("net in node must be unset");
		}
		node.setNet(this);
		addNodeInternal(node);
	}

	public void addNodes(Collection<Node> nodes) {
		nodes.forEach(this::addNode);
	}

	public boolean removeNode(Node node) {
		return nodes.remove(node);
	}

	public boolean removeNodeIf(Predicate<? super Node> filter) {
		return nodes.removeIf(filter);
	}

	public Set<Node> removeAllNodes() {
		HashSet<Node> formerNodes = new HashSet<>(this.nodes);
		formerNodes.forEach(n -> n.setNet(null));
		nodes.clear();
		return formerNodes;
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

	@VisibleForTesting
	void validateBelongsToNet(@NotNull Node node) {
		Validate.notNull(node);
		if (VALIDATE) {
			if (node.getNet() != this) {
				throw new IllegalStateException("node belongs to another net");
			}
			if (node != markerNode && !nodes.contains(node)) {
				throw new IllegalStateException("node does not belong to this net");
			}
		}
	}

	public MarkerNode permitMarkerNode() {
		this.markerNodePermitted = true;
		this.markerNode = new MarkerNode(this);
		return this.markerNode;
	}

	public void forbidMarkerNode() {
		if (VALIDATE) {
			consistencyCheck(); // check marker is not referenced
		}
		this.markerNodePermitted = false;
		this.markerNode = null;
	}

	public boolean isMarkerNodePermitted() {
		return markerNodePermitted;
	}

	public MarkerNode getMarkerNode() {
		return markerNode;
	}

	public void consistencyCheck() {
		if (root == null) {
			throw new AssertionError("Root is null");
		}
		if (!this.nodes.contains(root)) {
			throw new AssertionError("Root must be one of the net nodes");
		}
		for (Node node : nodes) {
			consistencyCheck(node);
		}
	}

	private void consistencyCheck(Node node) {

		if (node instanceof MarkerNode) {
			throw new AssertionError("Net node must not be a marker node");
		}
		if (node.getNet() != this) {
			throw new AssertionError("Node belongs to another net");
		}
		for (Arrow arrow : node.getArrows()) {
			consistencyCheck(arrow);
		}

		Hub hub = node.getHub();
		for (Arrow arrow : hub.getInverseReferences()) {
			if (!nodes.contains(arrow.getOrigin()))
				throw new AssertionError("Nodes referencing a net node must be part of the net");
			Node childOfParent = arrow.getTarget(Manner.DIRECT);
			if (childOfParent != node) {
				throw new AssertionError("Node must be child of its parent");
			}
		}
	}

	private void consistencyCheck(Arrow arrow) {
		Node child = arrow.getTarget(Manner.DIRECT);
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
		MarkerNode marker = permitMarkerNode();
		consumer.accept(marker);
		forbidMarkerNode();
	}

	public static void ancestorSwap(Node node1, Node node2) {
		Net net = node1.getNet();
		net.validateBelongsToNet(node2);
		Hub hub1 = node1.getHub();
		Hub hub2 = node2.getHub();
		node1.setHub(hub2);
		hub2.setNode(node1);
		node2.setHub(hub1);
		hub1.setNode(node2);
	}

	public UNode newUNode() {
		UNode uNode = new UNode(this);
		addNodeInternal(uNode);
		fireNewNodeCall(uNode);
		return uNode;
	}

	public UNode newUNode(Node child) {
		validateBelongsToNet(child);
		UNode uNode = new UNode(this, child);
		addNodeInternal(uNode);
		fireNewNodeCall(uNode);
		return uNode;
	}

	public BNode newBNode() {
		BNode bNode = new BNode(this);
		addNodeInternal(bNode);
		fireNewNodeCall(bNode);
		return bNode;
	}

	public BNode newBNode(Node leftChild, Node rightChild) {
		validateBelongsToNet(leftChild);
		validateBelongsToNet(rightChild);
		BNode bNode = new BNode(this, leftChild, rightChild);
		addNodeInternal(bNode);
		fireNewNodeCall(bNode);
		return bNode;
	}

	public CNode newCNode(Command command) {
		CNode cNode = new CNode(this, command);
		addNodeInternal(cNode);
		fireNewNodeCall(cNode);
		return cNode;
	}

	public void addNodeInternal(Node node) {
		nodes.add(node);
	}

	public void addExecutionEventListener(ExecutionEventListener listener) {
		eventListeners.add(listener);
	}

	public void removeExecutionEventListener(ExecutionEventListener listener) {
		eventListeners.remove(listener);
	}

	public void withExecutionEventListener(ExecutionEventListener listener, Runnable runnable) {
		addExecutionEventListener(listener);
		try {
			runnable.run();
		} finally {
			removeExecutionEventListener(listener);
		}
	}

	public void fireGetTargetCall(Node n, ArrowDirection arrowDirection, Manner manner) {
		eventListeners.forEach(listener -> listener.calledGetChild(n, arrowDirection, manner));
	}

	public void fireSetTargetCall(Node from, ArrowDirection arrowDirection, Node to, Manner manner) {
		validateBelongsToNet(to);
		eventListeners.forEach(listener -> listener.calledSetChild(from, arrowDirection, to, manner));
	}

	public void fireNewNodeCall(Node n) {
		eventListeners.forEach(listener -> listener.calledNewNode(n));
	}

}
