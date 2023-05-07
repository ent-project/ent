package org.ent.net.util;

import org.ent.net.Arrow;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;

import java.util.HashMap;
import java.util.Map;

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
			if (nodeOrig.isMarkerNode()) {
				throw new AssertionError("Marker node not expected");
			}
			originalToCloneMap.put(nodeOrig, netClone.newNode(nodeOrig.getValue()));
		}
	}

	private void copyMarker() {
		if (netOrig.isMarkerNodePermitted()) {
			netClone.permitMarkerNode();
		}
	}

	private void setRoot() {
		netClone.setRoot(originalToClone(netOrig.getRoot()));
	}

	private void setArrowTargets() {
		for (Node nodeOrig : netOrig.getNodes()) {
			for (Arrow arrowOrig : nodeOrig.getArrows()) {
				Arrow arrowClone = originalToClone(arrowOrig);
				Node targetClone = originalToClone(arrowOrig.getTarget(Purview.DIRECT));
				arrowClone.setTarget(targetClone, Purview.DIRECT);
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