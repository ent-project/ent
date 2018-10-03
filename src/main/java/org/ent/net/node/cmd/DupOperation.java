package org.ent.net.node.cmd;

import org.ent.net.Arrow;
import org.ent.net.NetController;
import org.ent.net.node.BNode;
import org.ent.net.node.CNode;
import org.ent.net.node.Node;
import org.ent.net.node.UNode;

public class DupOperation implements BiOperation<Arrow, Node> {

	@Override
	public void apply(NetController controller, Arrow setter, Node target) {
		Node copy;
		if (target instanceof CNode) {
			CNode targetCNode = (CNode) target;
			copy = controller.newCNode(targetCNode.getCommand());
		} else if (target instanceof UNode) {
			UNode targetUNode = (UNode) target;
			copy = controller.newUNode(targetUNode.getChild(controller));
		} else if (target instanceof BNode) {
			BNode targetBNode = (BNode) target;
			copy = controller.newBNode(targetBNode.getLeftChild(controller), targetBNode.getRightChild(controller));
		} else {
			throw new AssertionError("Unexpected Node type: " + target.getClass());
		}
		setter.setTarget(controller, copy);
	}

	@Override
	public String getShortName() {
		return "⏪";
	}

}
