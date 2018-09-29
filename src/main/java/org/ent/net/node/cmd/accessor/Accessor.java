package org.ent.net.node.cmd.accessor;

import java.util.Optional;

import org.ent.net.NetController;
import org.ent.net.node.Node;

public interface Accessor<T> {

	Optional<T> get(NetController controller, Node node);

	String getShortName();
}
