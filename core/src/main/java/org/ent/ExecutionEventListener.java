package org.ent;

import org.ent.net.ArrowDirection;
import org.ent.net.Manner;
import org.ent.net.node.Node;

public interface ExecutionEventListener {

	void calledGetChild(Node n, ArrowDirection arrowDirection, Manner manner);

	void calledSetChild(Node from, ArrowDirection arrowDirection, Node to, Manner manner);

	void calledNewNode(Node n);

}
