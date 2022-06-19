package org.ent;

import org.ent.net.ArrowDirection;
import org.ent.net.Purview;
import org.ent.net.node.Node;

public interface ExecutionEventListener {

	void calledGetChild(Node n, ArrowDirection arrowDirection, Purview purview);

	void calledSetChild(Node from, ArrowDirection arrowDirection, Node to, Purview purview);

	void calledNewNode(Node n);

}
