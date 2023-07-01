package org.ent.net.node;

/**
 * Hub is associated to a node and collects all references to it.
 *
 * Allows to efficiently swap two nodes X and Y by swapping the associated hubs. After this
 * operation, all arrows formerly pointing to X will then point to Y and vice versa.
 */
public class Hub {
	private Node node;

	public Hub(Node node) {
		this.node = node;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}
}
