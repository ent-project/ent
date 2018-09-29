package org.ent.net.node.cmd.accessor;

import java.util.Optional;

import org.ent.net.NetController;
import org.ent.net.node.Node;

public class NodeAccessor implements Accessor<Node> {

	@Override
	public Optional<Node> get(NetController controller, Node node) {
		return Optional.of(node);
	}

	@Override
	public String getShortName() {
		return "∗";
	}

}
