package org.ent;

import org.ent.net.ArrowDirection;
import org.ent.net.Purview;
import org.ent.net.node.Node;

public interface NetEventListener {

	void calledGetChild(Node n, ArrowDirection arrowDirection, Purview purview);

	void calledSetChild(Node from, ArrowDirection arrowDirection, Node to, Purview purview);

	void calledNewNode(Node n);

	void getValue(Node node, Purview purview);

	void setValue(Node node, int previousValue, int newValue);

	void evaluatedIsIdenticalCondition(Node node1, Node node2);
}
