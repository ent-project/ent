package org.ent.net.node.cmd.accessor;

import org.ent.Ent;
import org.ent.net.Arrow;
import org.ent.net.Purview;
import org.ent.net.node.Node;

public class FlowAccessor implements Accessor {

	@Override
	public int getCode() {
		return 0;
	}

	@Override
	public Arrow get(Node base, Ent ent, Purview purview) {
		return base.getRightArrow();
	}

	@Override
	public String getShortName() {
		return "^";
	}

	@Override
	public String toString() {
		return getShortName();
	}

}
