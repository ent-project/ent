package org.ent;

import org.ent.net.ArrowDirection;
import org.ent.net.Manner;
import org.ent.net.node.Node;

public interface ExecutionEventListener {

	void fireGetChild(Node n, ArrowDirection arrowDirection, Manner manner);

	void fireSetChild(Node from, ArrowDirection arrowDirection, Node to, Manner manner);

	void fireNewNode(Node n);

}
