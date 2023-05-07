package org.ent.dev.randnet;

import org.ent.net.Arrow;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.util.ModifiedPoisson;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class RandomNetCreator {

	private int numberOfNodes = 15;

	private final Random rand;

	private final CommandDrawing commandDrawing;

	private Net net;

	private DrawResult result;

	private enum DrawResult { SUCCESS, REJECT }

	public RandomNetCreator(Random rand, CommandDrawing commandDrawing) {
		this.rand = rand;
		this.commandDrawing = commandDrawing;
	}

	public Optional<Net> drawNet() {
		doDrawNet();
		return switch (result) {
			case SUCCESS -> Optional.of(net);
			case REJECT -> Optional.empty();
		};
	}

	private void doDrawNet() {
		initialize();
		createNodes();
		if (result == DrawResult.REJECT) {
			return;
		}
		rewireToRandomTargets();
		result = DrawResult.SUCCESS;
	}

	public int getNumberOfNodes() {
		return numberOfNodes;
	}

	public void setNumberOfNodes(int numberOfNodes) {
		this.numberOfNodes = numberOfNodes;
	}

	private void initialize() {
    	net = new Net();
    	result = null;
	}

	private void createNodes() {
		int noNodes = ModifiedPoisson.getModifiedPoisson(numberOfNodes).drawModifiedPoisson(rand);
		if (noNodes == 0) {
			result = DrawResult.REJECT;
			return;
		}
		for (int i = 0; i < noNodes; i++) {
			Node n = net.newNode(commandDrawing.drawCommand());
			if (i == 0) {
				net.setRoot(n);
			}
		}
	}

	private void rewireToRandomTargets() {
		List<Node> nodes = new ArrayList<>(net.getNodes());

		for (Node node : net.getNodes()) {
			for (Arrow arrow : node.getArrows()) {
				Node target = getRandomTarget(nodes);
				arrow.setTarget(target, Purview.DIRECT);
			}
		}
	}

	private Node getRandomTarget(List<Node> nodes) {
		int idx = rand.nextInt(nodes.size());
		return nodes.get(idx);
	}
}
