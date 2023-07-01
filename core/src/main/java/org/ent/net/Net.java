package org.ent.net;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.Validate;
import org.ent.MultiNetEventListener;
import org.ent.NetEventListener;
import org.ent.NopNetEventListener;
import org.ent.Profile;
import org.ent.net.node.BNode;
import org.ent.net.node.Hub;
import org.ent.net.node.MarkerNode;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.util.ReferentialGarbageCollection;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Net {

	private static final boolean VALIDATE = true;

	private final Set<Node> nodes;

	private int netIndex;

	private BiMap<Node, String> nodeNames;
	private final List<Node> nodesByIndex = new ArrayList<>();

	private int currentIndex;
	private Node root;

	private boolean markerNodePermitted;

	private MarkerNode markerNode;

	List<NetEventListener> eventListeners = new ArrayList<>(); // FIXME get rid

	NetEventListener netEventListener = new NopNetEventListener();

	private final AccessToken evalToken = new AccessToken();

	private boolean permittedToEvalRoot = true;
	private boolean permittedToWrite = true;

	public Net() {
		this.nodes = new LinkedHashSet<>();
	}

	public int getNetIndex() {
		return netIndex;
	}

	public Net setNetIndex(int netIndex) {
		this.netIndex = netIndex;
		return this;
	}

	public Net addEventListener(NetEventListener netEventListener) {
		if (this.netEventListener instanceof NopNetEventListener) {
			this.netEventListener = netEventListener;
		} else if (this.netEventListener instanceof MultiNetEventListener multiNetEventListener) {
			multiNetEventListener.addNetEventListener(netEventListener);
		} else {
			NetEventListener current = this.netEventListener;
			MultiNetEventListener multiNetEventListener = new MultiNetEventListener();
			multiNetEventListener.addNetEventListener(current);
			multiNetEventListener.addNetEventListener(netEventListener);
			this.netEventListener = multiNetEventListener;
		}
		return this;
	}

	public NetEventListener event() {
		return netEventListener;
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
		int index = node.getIndex();
		nodesByIndex.set(index, null);
		if (nodeNames != null) {
			nodeNames.remove(node);
		}
		return nodes.remove(node);
	}

	public void removeNodeIf(Predicate<? super Node> filter) {
		Iterator<Node> iterator = nodes.iterator();
		while (iterator.hasNext()) {
			Node node = iterator.next();
			if (filter.test(node)) {
				int index = node.getIndex();
				nodesByIndex.set(index, null);
				if (nodeNames != null) {
					nodeNames.remove(node);
				}
				iterator.remove();
			}
		}
	}

	public Set<Node> removeAllNodes() {
		HashSet<Node> formerNodes = new HashSet<>(this.nodes);
		formerNodes.forEach(n -> n.setNet(null));
		nodes.clear();
		nodesByIndex.clear();
		if (nodeNames != null) {
			nodeNames.clear();
		}
		return formerNodes;
	}

	public Node getRoot() {
		return root;
	}

	public void setRoot(Node root) {
		// FIXME check it is part of net
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
			Node childOfParent = arrow.getTarget(Purview.DIRECT);
			if (childOfParent != node) {
				throw new AssertionError("Node must be child of its parent");
			}
		}
	}

	private void consistencyCheck(Arrow arrow) {
		Node child = arrow.getTarget(Purview.DIRECT);
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

	public static void ancestorExchange(Node node1, Node node2) {
		Net net = node1.getNet();
		net.validateBelongsToNet(node2);
		Hub hub1 = node1.getHub();
		Hub hub2 = node2.getHub();
		node1.setHub(hub2);
		hub2.setNode(node1);
		node2.setHub(hub1);
		hub1.setNode(node2);
	}

	public Node newRoot() {
		Node newRoot = newNode();
		setRoot(newRoot);
		return newRoot;
	}

	public Node newRoot(int value, Node leftChild, Node rightChild) {
		Node newRoot = newNode(value, leftChild, rightChild);
		setRoot(newRoot);
		return newRoot;
	}

	public Node newRoot(Node leftChild, Node rightChild) {
		Node newRoot = newNode(leftChild, rightChild);
		setRoot(newRoot);
		return newRoot;
	}

	public Node newNode(int value, Node leftChild, Node rightChild) {
		validateBelongsToNet(leftChild);
		validateBelongsToNet(rightChild);
		BNode node = new BNode(this, value, leftChild, rightChild);
		addNodeInternal(node);
		fireNewNodeCall(node);
		return node;
	}

	public Node newNode() {
		BNode bNode = new BNode(this);
		addNodeInternal(bNode);
		fireNewNodeCall(bNode);
		return bNode;
	}

	public Node newNode(Node leftChild, Node rightChild) {
		validateBelongsToNet(leftChild);
		validateBelongsToNet(rightChild);
		BNode bNode = new BNode(this, leftChild, rightChild);
		addNodeInternal(bNode);
		fireNewNodeCall(bNode);
		return bNode;
	}

	public Node newNode(Command command) {
		return newNode(command.getValue());
	}

	public Node newNode(int value) {
		Node cNode = new BNode(this, value);
		addNodeInternal(cNode);
		fireNewNodeCall(cNode);
		return cNode;
	}

	public Node newUNode(Node child) {
		validateBelongsToNet(child);
		Node uNode = new BNode(this, child);
		addNodeInternal(uNode);
		fireNewNodeCall(uNode);
		return uNode;
	}

	public Node newCNode(Command command) {
		return newNode(command);
	}

	public Node newCNode(int value) {
		return newNode(value);
	}

	private void addNodeInternal(Node node) {
		nodes.add(node);
		node.setIndex(currentIndex);
		nodesByIndex.add(node);
		if (nodesByIndex.size() - 1 != currentIndex) {
			throw new AssertionError();
		}
		currentIndex++;
	}

	@Deprecated
	public void addExecutionEventListener(NetEventListener listener) {
		throw new NotImplementedException();
	}

	@Deprecated
	public void removeExecutionEventListener(NetEventListener listener) {
		throw new NotImplementedException();
	}

	@Deprecated
	public void withExecutionEventListener(NetEventListener listener, Runnable runnable) {
		addExecutionEventListener(listener);
		try {
			runnable.run();
		} finally {
			removeExecutionEventListener(listener);
		}
	}

	public void fireGetTargetCall(Node n, ArrowDirection arrowDirection, Purview purview) {
		if (purview == Purview.DIRECT) {
			return;
		}
		event().calledGetChild(n, arrowDirection, purview);
	}

	public void fireSetTargetCall(Node from, ArrowDirection arrowDirection, Node to, Purview purview) {
		if (Profile.PARANOIA) {
			validateBelongsToNet(to);
		}
		if (purview == Purview.DIRECT) {
			return;
		}
		event().calledSetChild(from, arrowDirection, to, purview);
	}

	public void fireNewNodeCall(Node n) {
		event().calledNewNode(n);
	}

	public void setName(Node node, String name) {
		validateBelongsToNet(node);
		if (nodeNames == null) {
			nodeNames = HashBiMap.create();
		}
		nodeNames.put(node, name);
	}

	public String getName(Node node) {
		if (nodeNames == null) {
			return null;
		}
		return nodeNames.get(node);
	}

	public Node getByName(String name) {
		if (nodeNames == null) {
			return null;
		}
		return nodeNames.inverse().get(name);
	}

	public List<Node> getNodesAsList() {
		return nodesByIndex;
	}

	public Node getNode(int index) {
		return nodesByIndex.get(index);
	}

	/**
	 * Provide token for elevated access rights. Holder of the eval token
	 * can modify this Net, even if modification is not permitted in general.
	 */
	public AccessToken getEvalToken() {
		return evalToken;
	}

	public boolean isPermittedToEval(Node node) {
		return permittedToWrite || (permittedToEvalRoot && node == root);
	}

	public boolean isPermittedToEvalRoot() {
		return permittedToEvalRoot;
	}

	public Net setPermittedToEvalRoot(boolean permittedToEvalRoot) {
		this.permittedToEvalRoot = permittedToEvalRoot;
		return this;
	}

	public boolean isPermittedToWrite(AccessToken accessToken) {
		if (permittedToWrite) {
			return true;
		}
		return permittedToEvalRoot && accessToken == this.evalToken;
	}

	public Net setPermittedToWrite(boolean permittedToWrite) {
		this.permittedToWrite = permittedToWrite;
		return this;
	}
}
