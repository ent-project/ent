package org.ent.net.node.cmd.accessor;

import org.ent.net.Arrow;
import org.ent.net.node.Node;
import org.ent.permission.Permissions;

public interface Accessor {

	Arrow get(Node base, Permissions permissions);

	default
	Node getTarget(Node base, Permissions permissions) {
		Arrow arrow = get(base, permissions);
		return arrow.getTarget(permissions);
	}

	int getCode();

	String getShortName();
}
