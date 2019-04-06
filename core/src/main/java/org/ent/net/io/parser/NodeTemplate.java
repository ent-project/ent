package org.ent.net.io.parser;

import org.ent.net.ArrowDirection;
import org.ent.net.NetController;
import org.ent.net.node.Node;

interface NodeTemplate {

	Node generateNode(NetController controller, Node childPlaceholder) throws ParserException;

	NodeTemplate getChild(ArrowDirection arrowDirection);
}