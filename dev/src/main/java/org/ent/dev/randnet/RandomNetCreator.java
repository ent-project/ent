package org.ent.dev.randnet;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.net.Arrow;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.util.ModifiedPoisson;

import java.util.Optional;

public class RandomNetCreator {
	public static final int MAX_ATTEMPTS = 1000;

	private final int numberOfNodes;

	private final UniformRandomProvider rand;

	private final DefaultValueDrawing valueDrawing;

	private Net net;

	private DrawResult result;

	private enum DrawResult { SUCCESS, REJECT }

	public RandomNetCreator(int numberOfNodes, UniformRandomProvider rand, DefaultValueDrawing valueDrawing) {
		this.numberOfNodes = numberOfNodes;
		this.rand = rand;
		this.valueDrawing = valueDrawing;
	}

	public Net drawNet() {
		for (int i = 0; i < MAX_ATTEMPTS; i++) {
			Optional<Net> optionalNet = drawNetMaybe();
			if (optionalNet.isPresent()) {
				return optionalNet.get();
			}
		}
		throw new IllegalStateException("Max attempts exceeded. Wrong configuration or very bad luck.");
	}

	public Optional<Net> drawNetMaybe() {
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
			Node n = net.newNode(valueDrawing.drawValue(rand));
			if (i == 0) {
				net.setRoot(n);
			}
		}
	}

	private void rewireToRandomTargets() {
		int size = net.getNodes().size();
		for (Node node : net.getNodes()) {
			for (Arrow arrow : node.getArrows()) {
				int targetIndex = rand.nextInt(size);
				Node target = net.getNode(targetIndex);
				arrow.setTarget(target, Purview.DIRECT);
			}
		}
	}
}
