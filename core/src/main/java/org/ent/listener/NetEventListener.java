package org.ent.listener;

import org.ent.net.ArrowDirection;
import org.ent.net.node.Node;

public interface NetEventListener {

	void calledGetChild(Node n, ArrowDirection arrowDirection);

	void calledSetChild(Node from, ArrowDirection arrowDirection, Node to);

	void calledNewNode(Node n);

	void getValue(Node node);

	void setValue(Node node, int previousValue, int newValue);

	void ancestorExchange(Node node1, Node node2);

	void beforeEvalExecution(Node target, boolean flow);

    void setRoot(Node previousRoot, Node newRoot);
}
