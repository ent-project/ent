package org.ent;

import org.ent.net.ArrowDirection;
import org.ent.net.ExecutionContext;
import org.ent.net.node.Node;

public interface ExecutionEventListener {

	void fireExecutionStart();

	void fireGetChild(Node n, ArrowDirection arrowDirection, ExecutionContext context);

	void fireSetChild(Node from, ArrowDirection arrowDirection, Node to, ExecutionContext context);

	void fireNewNode(Node n);

}
