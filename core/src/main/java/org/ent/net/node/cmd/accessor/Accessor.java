package org.ent.net.node.cmd.accessor;

import java.util.Optional;

import org.ent.net.Manner;
import org.ent.net.node.Node;

public interface Accessor<T> {

	Optional<T> get(Node node, Manner manner);

	String getShortName();

	String getShortNameAscii();
}
