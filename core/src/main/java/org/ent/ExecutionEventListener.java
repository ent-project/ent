package org.ent;

import org.ent.net.ArrowDirection;
import org.ent.net.node.CNode;
import org.ent.net.node.Node;

public interface ExecutionEventListener {

	void fireExecutionStart();

	void fireGetChild(Node n, ArrowDirection arrowDirection);

	void fireSetChild(Node from, ArrowDirection arrowDirection, Node to);

	void fireNewNode(Node n);

}
