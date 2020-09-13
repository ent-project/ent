package org.ent.dev.randnet;

import java.util.List;
import java.util.Random;

import org.ent.net.node.cmd.Command;
import org.ent.util.Tools;

public class CommandDrawingImpl implements CommandDrawing {

	private final Random rand;

	private final List<CommandCandidate> commandCandidates;

	private final double[] summedWeights;

	private final double totalWeight;

	public CommandDrawingImpl(Random rand, List<CommandCandidate> commandCandidates) {
		this.rand = rand;
		this.commandCandidates = commandCandidates;
		this.summedWeights = new double[commandCandidates.size()];
		double weightSum = 0;
		for (int i = 0; i < commandCandidates.size(); i++) {
			weightSum += commandCandidates.get(i).weight();
			summedWeights[i] = weightSum;
		}
		this.totalWeight = weightSum;
	}

	@Override
	public Command drawCommand() {
		double p = rand.nextDouble() * totalWeight;
		int idx = randomValueToIndex(p);
		return commandCandidates.get(idx).command();
	}

	int randomValueToIndex(double p) {
		return Tools.binarySearchLowerEqual(summedWeights, p, false);
	}

}
