package org.ent.dev.plan;

import static org.ent.net.io.HexConverter.toHex;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.ent.dev.CopyReplicator;
import org.ent.dev.plan.DataProperties.PropNet;
import org.ent.dev.plan.DataProperties.PropReplicator;
import org.ent.dev.plan.DataProperties.PropSeed;
import org.ent.dev.randnet.CommandCandidate;
import org.ent.dev.randnet.CommandDrawing;
import org.ent.dev.randnet.CommandDrawingImpl;
import org.ent.dev.randnet.RandomNetCreator;
import org.ent.dev.unit.DataImpl;
import org.ent.dev.unit.Source;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.node.cmd.CommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomNetSource implements Source {

	private static final Logger log = LoggerFactory.getLogger(RandomNetSource.class);

	private static final int RANDOM_NET_CREATOR_NUMBER_OF_NODES = 15;

	private static final double RANDOM_NET_CREATOR_FRACTION_CNODES = 0.2;

	private static final double RANDOM_NET_CREATOR_FRACTION_UNODES = 0.5;

	private static final double RANDOM_NET_CREATOR_FRACTION_BNODES = 0.3;

	private static final int LEVEL0_SEARCH_LIMIT = 100_000;

	private final Random randNetSeeds;

	private List<CommandCandidate> commandCandidates;

	public class RandomNetSourceData extends DataImpl implements PropNet, PropSeed, PropReplicator {

		public RandomNetSourceData(Net net, long seed) {
			setNet(net);
			setSeed(seed);
			setReplicator(new CopyReplicator(net));
		}

		public Net getNewSpecimenFromSeed() {
			return drawNet(getSeed()).get();
		}

		public void log() {
			if (log.isTraceEnabled()) {
				NetFormatter formatter = new NetFormatter();
				log.trace("#{} {}", toHex(getSeed()), formatter.format(getNet()));
			}
		}
	}

	public RandomNetSource(Random randNetSeeds) {
		this.randNetSeeds = randNetSeeds;
		this.commandCandidates = setupCommandCandidates();
	}

	@Override
	public RandomNetSourceData get() {
		for (int tries = 0; tries < LEVEL0_SEARCH_LIMIT; tries++) {
			long netSeed = randNetSeeds.nextLong();
			Optional<Net> maybeNet = drawNet(netSeed);
			if (maybeNet.isPresent()) {
				RandomNetSourceData netInfo = new RandomNetSourceData(maybeNet.get(), netSeed);
				netInfo.log();
				return netInfo;
			} else {
				logReject(netSeed);
			}
		}
		throw new RuntimeException("Level0 search limit exceeded (" + LEVEL0_SEARCH_LIMIT + ")");
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

	private void logReject(long seed) {
		if (log.isTraceEnabled()) {
			log.trace("#{} :reject", toHex(seed));
		}
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
