package org.ent.net.node;

import java.util.HashSet;
import java.util.Set;

import org.ent.net.Arrow;

/**
 * Hub is associated to a node and collects all references to it.
 *
 * Allows to efficiently swap two nodes X and Y by swapping the associated hubs. After this
 * operation, all arrows formerly pointing to X will then point to Y and vice versa.
 */
public class Hub {
	private Node node;
	private final Set<Arrow> inverseReferences;

	public Hub(Node node) {
		this.node = node;
		this.inverseReferences = new HashSet<>();
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public void addInverseReference(Arrow arrow) {
		inverseReferences.add(arrow);
	}

	public void removeInverseReference(Arrow arrow) {
		inverseReferences.remove(arrow);
	}

	public Set<Arrow> getInverseReferences() {
		return inverseReferences;
	}
}
