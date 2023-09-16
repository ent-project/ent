package org.ent.net.node.cmd.accessor;

import org.ent.net.Arrow;
import org.ent.net.node.Node;
import org.ent.permission.Permissions;

public class FlowAccessor implements Accessor {

	@Override
	public int getCode() {
		return 0;
	}

	@Override
	public Arrow get(Node base, Permissions permissions) {
		return base.getRightArrow();
	}

	@Override
	public String getShortName() {
		return "\\";
	}

	@Override
	public String toString() {
		return getShortName();
	}

}
