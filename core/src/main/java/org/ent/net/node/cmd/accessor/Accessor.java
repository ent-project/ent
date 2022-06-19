package org.ent.net.node.cmd.accessor;

import java.util.Optional;

import org.ent.net.Purview;
import org.ent.net.node.Node;

public interface Accessor<T> {

	Optional<T> get(Node node, Purview purview);

	String getShortName();

	String getShortNameAscii();
}
