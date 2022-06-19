package org.ent.dev.randnet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.ent.net.Arrow;
import org.ent.net.Purview;
import org.ent.net.Net;
import org.ent.net.node.Node;
import org.ent.util.ModifiedPoisson;

public class RandomNetCreator {

	private int numberOfNodes = 15;

	private double fractionBNodes = 0.3;

	private double fractionUNodes = 0.5;

	private double fractionCNodes = 0.2;

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
		switch (result) {
			case SUCCESS: return Optional.of(net);
			case REJECT: return Optional.empty();
			default: throw new AssertionError("Expected result value, but was null");
		}
	}

	private void doDrawNet() {
		initialize();
		createNodes();
		if (result == DrawResult.REJECT) return;
		rewireToRandomTargets();
		result = DrawResult.SUCCESS;
	}

	public int getNumberOfNodes() {
		return numberOfNodes;
	}

	public void setNumberOfNodes(int numberOfNodes) {
		this.numberOfNodes = numberOfNodes;
	}

	public double getFractionBNodes() {
		return fractionBNodes;
	}

	public void setFractionBNodes(double fractionBNodes) {
		this.fractionBNodes = fractionBNodes;
	}

	public double getFractionUNodes() {
		return fractionUNodes;
	}

	public void setFractionUNodes(double fractionUNodes) {
		this.fractionUNodes = fractionUNodes;
	}

	public double getFractionCNodes() {
		return fractionCNodes;
	}

	public void setFractionCNodes(double fractionCNodes) {
		this.fractionCNodes = fractionCNodes;
	}

	private void initialize() {
    	net = new Net();
    	result = null;
	}

	private void createNodes() {
		int noBNodes = ModifiedPoisson.getModifiedPoisson(numberOfNodes * fractionBNodes).drawModifiedPoisson(rand);
		if (noBNodes == 0) {
			result = DrawResult.REJECT;
			return;
		}
		int rootIdx = rand.nextInt(noBNodes);
		for (int i = 0; i < noBNodes; i++) {
			Node n = net.newBNode();
			if (i == rootIdx) {
				net.setRoot(n);
			}
		}

		int noUNodes = ModifiedPoisson.getModifiedPoisson(numberOfNodes * fractionUNodes).drawModifiedPoisson(rand);
		for (int i = 0; i < noUNodes; i++) {
			net.newUNode();
		}

		int noCNodes = ModifiedPoisson.getModifiedPoisson(numberOfNodes * fractionCNodes).drawModifiedPoisson(rand);
		for (int i = 0; i < noCNodes; i++) {
			net.newCNode(commandDrawing.drawCommand());
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
