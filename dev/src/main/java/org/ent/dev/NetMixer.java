package org.ent.dev;

import org.ent.net.Arrow;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.MarkerNode;
import org.ent.net.node.Node;
import org.ent.util.ModifiedPoisson;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class NetMixer {

	private double rewireFraction = 0.2;

	private final Random rand;

	private final Net primaryNet;

	private final Net joiningNet;

	private List<Node> nodesList;

	public NetMixer(Random rand, Net primaryNet, Net joiningNet) {
		this.rand = rand;
		this.primaryNet = primaryNet;
		this.joiningNet = joiningNet;
		validate();
	}

	public double getRewireFraction() {
		return rewireFraction;
	}

	public void setRewireFraction(double rewireFraction) {
		this.rewireFraction = rewireFraction;
	}

	private void validate() {
		if (joiningNet.isMarkerNodePermitted() && !primaryNet.isMarkerNodePermitted()) {
			throw new IllegalArgumentException("joining net permits marker nodes, but primary net does not");
		}
	}

	public void join() {
		doJoinNets();
		randomlyWireArrowsToMarkerNodes();
		randomlyRewireArrows();
	}

	private void doJoinNets() {
		List<Node> joiningNodes = joiningNet.removeAllNodes();
		primaryNet.addNodes(joiningNodes);
		nodesList = new ArrayList<>(primaryNet.getNodes());
	}

	private void randomlyWireArrowsToMarkerNodes() {
		for (Node node : nodesList) {
			for (Arrow arrow : node.getArrows()) {
				Node target = arrow.getTarget(Purview.DIRECT);
				if (target instanceof MarkerNode) {
					Node newTarget = drawRandomNonMarkerNode();
					arrow.setTarget(newTarget, Purview.DIRECT);
				}
			}
		}
	}

	private void randomlyRewireArrows() {
		int numToRewire = ModifiedPoisson.getModifiedPoisson(nodesList.size() * rewireFraction)
				.drawModifiedPoisson(rand);

		for (int i = 0; i < numToRewire; i++) {
			Node node = drawRandomNonMarkerNode();
			Optional<Arrow> randArrow = drawRandomArrow(node);
			randArrow.ifPresent(arrow -> {
				Node target = drawRandomNonMarkerNode();
				arrow.setTarget(target, Purview.DIRECT);
			});
		}
	}

	private Optional<Arrow> drawRandomArrow(Node node) {
		int numArrows = node.getArrows().size();
		if (numArrows == 0) {
			return Optional.empty();
		} else if (numArrows == 1) {
			return Optional.of(node.getArrows().get(0));
		} else {
			int index = rand.nextInt(numArrows);
			return Optional.of(node.getArrows().get(index));
		}
	}

	private Node drawRandomNonMarkerNode() {
		int idx = rand.nextInt(nodesList.size());
		return nodesList.get(idx);
	}
}
