package org.ent.net;

import org.ent.net.node.BNode;
import org.ent.net.node.Node;
import org.ent.net.node.UNode;

public interface NetController {

	Node getTarget(Arrow arrow);

	void setTarget(Arrow arrow, Node target);

	UNode newUNode(Node child);

	BNode newBNode(Node leftChild, Node rightChild);

	void referenceSwap(Node x, Node y);
}
