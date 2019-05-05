package org.ent.net;

import org.ent.net.node.BNode;
import org.ent.net.node.CNode;
import org.ent.net.node.Node;
import org.ent.net.node.UNode;
import org.ent.net.node.cmd.Command;

public interface NetController {

	Node getTarget(Arrow arrow);

	void setTarget(Arrow arrow, Node target);

	UNode newUNode();

	UNode newUNode(Node child);

	BNode newBNode();

	BNode newBNode(Node leftChild, Node rightChild);

	CNode newCNode(Command command);

	void ancestorSwap(Node x, Node y);
}
