package org.ent.net.node.cmd.accessor;

import java.util.Optional;

import org.ent.net.Manner;
import org.ent.net.node.Node;

public class NodeAccessor implements Accessor<Node> {

	@Override
	public Optional<Node> get(Node node, Manner manner) {
		return Optional.of(node);
	}

	@Override
	public String getShortName() {
		return "•";
	}

	@Override
	public String getShortNameAscii() {
		return "*";
	}
}
