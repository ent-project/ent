package org.ent.net.node;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Node {

	protected Net net;

	private Hub hub;

	protected Node(Net net) {
		this.net = net;
		this.hub = new Hub(this);
	}

	public Net getNet() {
		return net;
	}

	public void setNet(Net net) {
		this.net = net;
	}

	public Hub getHub() {
		return hub;
	}

	public void setHub(Hub hub) {
		this.hub = hub;
	}

	public abstract List<Arrow> getArrows();

	public abstract Arrow getArrow(ArrowDirection arrowDirection);

	public abstract Optional<Arrow> getArrowMaybe(ArrowDirection arrowDirection);

	public abstract <T> T instanceOf(Function<CNode, T> cNodeCase, Function<UNode, T> uNodeCase, Function<BNode, T> bNodeCase);

	public void doInstanceOf(Consumer<CNode> cNodeCase, Consumer<UNode> uNodeCase, Consumer<BNode> bNodeCase) {
		this.instanceOf(
				cNode -> {
					cNodeCase.accept(cNode);
					return null;
					},
				uNode -> {
					uNodeCase.accept(uNode);
					return null;
					},
				bNode -> {
					bNodeCase.accept(bNode);
					return null;
				});
	}

}
