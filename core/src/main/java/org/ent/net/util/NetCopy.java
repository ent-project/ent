package org.ent.net.util;

import java.util.HashMap;
import java.util.Map;

import org.ent.net.Arrow;
import org.ent.net.DefaultNetController;
import org.ent.net.Net;
import org.ent.net.NetController;
import org.ent.net.node.BNode;
import org.ent.net.node.CNode;
import org.ent.net.node.MarkerNode;
import org.ent.net.node.Node;
import org.ent.net.node.UNode;

public class NetCopy {

	private final Net netOrig;

	private final Net netClone;

	private final NetController controllerOrig;

	private final NetController controllerClone;

	private final Map<Node, Node> originalToCloneMap;

	public NetCopy(Net original) {
		this.netOrig = original;
		this.netClone = new Net();
		this.controllerOrig = new DefaultNetController(netOrig);
		this.controllerClone = new DefaultNetController(netClone);
		this.originalToCloneMap = new HashMap<>();
	}

	public Net createCopy() {
		copyNodes();
		setRoot();
		setArrowTargets();
		return netClone;
	}

	private void copyNodes() throws AssertionError {
		MarkerNode marker = new MarkerNode();
		for (Node nodeOrig : netOrig.getNodes()) {
			Node nodeClone;
			if (nodeOrig instanceof CNode) {
				CNode cNodeOrig = (CNode) nodeOrig;
				nodeClone = controllerClone.newCNode(cNodeOrig.getCommand());
			} else if (nodeOrig instanceof UNode) {
				nodeClone = controllerClone.newUNode(marker);
			} else if (nodeOrig instanceof BNode) {
				nodeClone = controllerClone.newBNode(marker, marker);
			} else {
				throw new AssertionError("Unexpected Node type: " + nodeOrig.getClass());
			}
			originalToCloneMap.put(nodeOrig, nodeClone);
		}
	}

	private void setRoot() {
		netClone.setRoot(originalToClone(netOrig.getRoot()));
	}

	private void setArrowTargets() {
		for (Node nodeOrig : netOrig.getNodes()) {
			for (Arrow arrowOrig : nodeOrig.getArrows()) {
				Arrow arrowClone = originalToClone(arrowOrig);
				Node targetClone = originalToClone(arrowOrig.getTarget(controllerOrig));
				controllerClone.setTarget(arrowClone, targetClone);
			}
		}
	}

	public Net getOriginalNet() {
		return netOrig;
	}

	public Net getClonedNet() {
		return netClone;
	}

	public Node originalToClone(Node nodeOrig) {
		return originalToCloneMap.get(nodeOrig);
	}

	public Arrow originalToClone(Arrow arrowOrig) {
		Node nodeOrig = arrowOrig.getOrigin();
		Node nodeClone = originalToCloneMap.get(nodeOrig);
		if (nodeClone == null) {
			throw new AssertionError("Corresponding node cannot be found in copy");
		}
		return nodeClone.getArrow(arrowOrig.getDirection());
	}
}