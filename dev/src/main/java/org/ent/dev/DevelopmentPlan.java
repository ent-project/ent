package org.ent.dev;

import static org.ent.net.io.HexConverter.toHex;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.ent.dev.randnet.CommandCandidate;
import org.ent.dev.randnet.CommandDrawing;
import org.ent.dev.randnet.CommandDrawingImpl;
import org.ent.dev.randnet.RandomNetCreator;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.node.cmd.CommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DevelopmentPlan {

	private static final Logger logPlain = LoggerFactory.getLogger("plain");

	private static final long RANDOM_MASTER_SEED = 0xfa1afeL;

	private static final int RANDOM_NET_CREATOR_NUMBER_OF_NODES = 15;

	private static final double RANDOM_NET_CREATOR_FRACTION_CNODES = 0.2;

	private static final double RANDOM_NET_CREATOR_FRACTION_UNODES = 0.5;

	private static final double RANDOM_NET_CREATOR_FRACTION_BNODES = 0.3;

	private Random randMaster;

	private List<CommandCandidate> commandCandidates;

	public static void main(String[] args) throws Exception {
		DevelopmentPlan plan = new DevelopmentPlan();
		plan.execute();
	}

	public void execute() {
		initialize();

		Random randNetCreatorSeeds = newRandom();
		for (int i = 1; i <= 100; i++) {
			long seed = randNetCreatorSeeds.nextLong();
			Optional<Net> drawnNet = drawNet(seed);
			if (drawnNet.isPresent()) {
				NetFormatter formatter = new NetFormatter();
				logPlain.info("{} #{} {}", String.format("%3d", i), toHex(seed), formatter.format(drawnNet.get()));
			} else {
				logPlain.info("{} #{} :reject", String.format("%3d", i), toHex(seed));
			}
		}
	}

	private void initialize() {
		randMaster = new Random(RANDOM_MASTER_SEED);
		commandCandidates = setupCommandCandidates();
	}

	private Optional<Net> drawNet(long netSeed) {
		Random rand = new Random(netSeed);
		CommandDrawing drawing = new CommandDrawingImpl(rand, commandCandidates);
		RandomNetCreator creator = new RandomNetCreator(rand, drawing);
		creator.setNumberOfNodes(RANDOM_NET_CREATOR_NUMBER_OF_NODES);
		creator.setFractionBNodes(RANDOM_NET_CREATOR_FRACTION_BNODES);
		creator.setFractionUNodes(RANDOM_NET_CREATOR_FRACTION_UNODES);
		creator.setFractionCNodes(RANDOM_NET_CREATOR_FRACTION_CNODES);
		return creator.drawNet();
	}

	private Random newRandom() {
		return new Random(randMaster.nextLong());
	}

	private List<CommandCandidate> setupCommandCandidates() {
		List<CommandCandidate> commandCandidates = new ArrayList<>();

		double N1 = 1.0;
		double N3 = N1 / 3;
		double N9 = N1 / 9;
		int MAX_EVAL_LEVEL = 5;
		double N_EVAL = N1 / MAX_EVAL_LEVEL;

		commandCandidates.add(new CommandCandidate(CommandFactory.createNopCommand(), N1));
		for (ArrowDirection left : ArrowDirection.values()) {
			commandCandidates.add(new CommandCandidate(CommandFactory.createSetCommandL(left), N3));
			for (ArrowDirection right : ArrowDirection.values()) {
				commandCandidates.add(new CommandCandidate(CommandFactory.createSetCommandLR(left, right), N9));
			}
		}
		for (ArrowDirection left : ArrowDirection.values()) {
			commandCandidates.add(new CommandCandidate(CommandFactory.createDupCommand(left), N3));
		}
		commandCandidates.add(new CommandCandidate(CommandFactory.createAncestorSwapCommand(), N1));
		for (int evalLevel = 1; evalLevel <= MAX_EVAL_LEVEL; evalLevel++) {
			commandCandidates.add(new CommandCandidate(CommandFactory.createEvalCommand(evalLevel), N_EVAL));
		}
		for (ArrowDirection left : ArrowDirection.values()) {
			for (ArrowDirection right : ArrowDirection.values()) {
				commandCandidates.add(new CommandCandidate(CommandFactory.createIsIdenticalCommand(left, right), N9));
			}
		}
		return commandCandidates;
	}
}
