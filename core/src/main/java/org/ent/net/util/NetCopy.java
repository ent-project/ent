package org.ent.net.util;

import java.util.HashMap;
import java.util.Map;

import org.ent.net.Arrow;
import org.ent.net.Manner;
import org.ent.net.Net;
import org.ent.net.node.BNode;
import org.ent.net.node.CNode;
import org.ent.net.node.MarkerNode;
import org.ent.net.node.Node;
import org.ent.net.node.UNode;

public class NetCopy {

	private final Net netOrig;

	private final Net netClone;

	private final Map<Node, Node> originalToCloneMap;

	public NetCopy(Net original) {
		this.netOrig = original;
		this.netClone = new Net();
		this.originalToCloneMap = new HashMap<>();
	}

	public Net createCopy() {
		copyNodes();
		copyMarker();
		setRoot();
		setArrowTargets();
		return netClone;
	}

	private void copyNodes() throws AssertionError {
		for (Node nodeOrig : netOrig.getNodes()) {
			Node nodeClone;
			if (nodeOrig instanceof CNode cNodeOrig) {
				nodeClone = netClone.newCNode(cNodeOrig.getCommand());
			} else if (nodeOrig instanceof UNode) {
				nodeClone = netClone.newUNode();
			} else if (nodeOrig instanceof BNode) {
				nodeClone = netClone.newBNode();
			} else {
				throw new AssertionError("Unexpected Node type: " + nodeOrig.getClass());
			}
			originalToCloneMap.put(nodeOrig, nodeClone);
		}
	}

	private void copyMarker() {
		if (netOrig.isMarkerNodePermitted()) {
			MarkerNode markerClone = netClone.permitMarkerNode();
		}
	}

	private void setRoot() {
		netClone.setRoot(originalToClone(netOrig.getRoot()));
	}

	private void setArrowTargets() {
		for (Node nodeOrig : netOrig.getNodes()) {
			for (Arrow arrowOrig : nodeOrig.getArrows()) {
				Arrow arrowClone = originalToClone(arrowOrig);
				Node targetClone = originalToClone(arrowOrig.getTarget(Manner.DIRECT));
				arrowClone.setTarget(targetClone, Manner.DIRECT);
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
		Node nodeClone;
		if (netOrig.isMarkerNodePermitted() && nodeOrig == netOrig.getMarkerNode()) {
			return netClone.getMarkerNode();
		} else {
			nodeClone = originalToCloneMap.get(nodeOrig);
		}
		if (nodeClone == null) {
			throw new AssertionError("Corresponding node cannot be found in copy");
		}
		return nodeClone;
	}

	public Arrow originalToClone(Arrow arrowOrig) {
		Node nodeOrig = arrowOrig.getOrigin();
		Node nodeClone = originalToClone(nodeOrig);
		return nodeClone.getArrow(arrowOrig.getDirection());
	}
}