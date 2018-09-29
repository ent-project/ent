package org.ent.net.node.cmd;

import org.ent.net.Arrow;
import org.ent.net.NetController;
import org.ent.net.node.Node;

public class SetOperation implements BiOperation<Arrow, Node> {

	@Override
	public void apply(NetController controller, Arrow setter, Node target) {
		setter.setTarget(controller, target);
	}

	@Override
	public String getShortName() {
		return "◄";
	}

}
