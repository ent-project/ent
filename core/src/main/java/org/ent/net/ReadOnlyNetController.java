package org.ent.net;

import org.ent.net.node.BNode;
import org.ent.net.node.CNode;
import org.ent.net.node.Node;
import org.ent.net.node.UNode;
import org.ent.net.node.cmd.Command;

public class ReadOnlyNetController implements NetController {

	private static final ReadOnlyNetController instance = new ReadOnlyNetController();

	public static ReadOnlyNetController getInstance() {
		return instance;
	}

	@Override
	public Node getTarget(Arrow arrow) {
		return arrow.getTargetForNetControllerOnly();
	}

	@Override
	public void setTarget(Arrow arrow, Node target) {
		throw new UnsupportedOperationException();
	}

	@Override
	public UNode newUNode(Node child) {
		throw new UnsupportedOperationException();
	}

	@Override
	public BNode newBNode(Node leftChild, Node rightChild) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CNode newCNode(Command command) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void ancestorSwap(Node x, Node y) {
		throw new UnsupportedOperationException();
	}

}
