package org.ent.dev.plan;

import org.ent.Ent;
import org.ent.dev.CopyReplicator;
import org.ent.dev.plan.DataProperties.PropEnt;
import org.ent.dev.plan.DataProperties.PropReplicator;
import org.ent.dev.plan.DataProperties.PropSeed;
import org.ent.dev.randnet.CommandCandidate;
import org.ent.dev.randnet.CommandDrawing;
import org.ent.dev.randnet.CommandDrawingImpl;
import org.ent.dev.randnet.RandomNetCreator;
import org.ent.dev.unit.data.DataImpl;
import org.ent.dev.unit.local.Source;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.accessor.Accessors;
import org.ent.net.node.cmd.operation.Operations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.ent.net.io.HexConverter.toHex;

public class RandomNetSource implements Source {

	private static final Logger log = LoggerFactory.getLogger(RandomNetSource.class);

	private int noNodes = 15;

	private static final int LEVEL0_SEARCH_LIMIT = 100_000;

	private final Random randNetSeeds;

	private final List<CommandCandidate> commandCandidates;

	public static class RandomNetSourceData extends DataImpl implements PropEnt, PropSeed, PropReplicator {

		public RandomNetSourceData(Ent ent, long seed) {
			setEnt(ent);
			setSeed(seed);
			setReplicator(new CopyReplicator(ent));
		}

		public void log() {
			if (log.isTraceEnabled()) {
				NetFormatter formatter = new NetFormatter();
				log.trace("#{} {}", toHex(getSeed()), formatter.format(getEnt()));
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
				Ent ent = new Ent(maybeNet.get()); // FIXME
				RandomNetSourceData netInfo = new RandomNetSourceData(ent, netSeed);
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
		creator.setNumberOfNodes(noNodes);
		return creator.drawNet();
	}

	private void logReject(long seed) {
		if (log.isTraceEnabled()) {
			log.trace("#{} :reject", toHex(seed));
		}
	}

	private List<CommandCandidate> setupCommandCandidates() {
		List<CommandCandidate> candidates = new ArrayList<>();

		double N1 = 1.0;
		double N3 = N1 / 3;
		double N9 = N1 / 9;

		candidates.add(new CommandCandidate(Commands.NOP, N1));
		for (ArrowDirection direction1 : ArrowDirection.values()) {
			candidates.add(new CommandCandidate(Commands.get(Operations.SET_OPERATION, Accessors.get(direction1), Accessors.DIRECT), N3));
			for (ArrowDirection direction2 : ArrowDirection.values()) {
				candidates.add(new CommandCandidate(Commands.get(Operations.SET_OPERATION, Accessors.get(direction1), Accessors.get(direction2)), N9));
			}
		}
		for (ArrowDirection direction : ArrowDirection.values()) {
			candidates.add(new CommandCandidate(Commands.get(Operations.DUP_OPERATION, Accessors.get(direction), Accessors.DIRECT), N3));
		}
		candidates.add(new CommandCandidate(Commands.ANCESTOR_EXCHANGE, N1));
		for (ArrowDirection direction1 : ArrowDirection.values()) {
			for (ArrowDirection direction2 : ArrowDirection.values()) {
				candidates.add(new CommandCandidate(Commands.get(Operations.IS_IDENTICAL_OPERATION, Accessors.get(direction1), Accessors.get(direction2)), N9));
			}
		}
		return candidates;
	}

	public int getNoNodes() {
		return noNodes;
	}

	public void setNoNodes(int noNodes) {
		this.noNodes = noNodes;
	}

}
