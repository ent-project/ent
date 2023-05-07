package org.ent.net.io.parser;

import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.node.Node;

interface NodeTemplate {

	Node generateNode(Net net) throws ParserException;

	NodeTemplate getChild(ArrowDirection arrowDirection);

	void setChild(ArrowDirection arrowDirection, NodeTemplate child);
}