package org.ent.dev.plan;

import org.ent.Ent;
import org.ent.dev.CopyReplicator;
import org.ent.dev.plan.DataProperties.PropEnt;
import org.ent.dev.plan.DataProperties.PropReplicator;
import org.ent.dev.plan.DataProperties.PropSeed;
import org.ent.dev.randnet.DefaultValueDrawing;
import org.ent.dev.randnet.RandomNetCreator;
import org.ent.dev.randnet.ValueDrawing;
import org.ent.dev.unit.data.DataImpl;
import org.ent.dev.unit.local.Source;
import org.ent.net.Net;
import org.ent.net.io.formatter.NetFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Random;

import static org.ent.net.io.HexConverter.toHex;

public class RandomNetSource implements Source {

	private static final Logger log = LoggerFactory.getLogger(RandomNetSource.class);

	private int noNodes = 15;

	private static final int LEVEL0_SEARCH_LIMIT = 100_000;

	private final Random randNetSeeds;

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
	}

	@Override
	public RandomNetSourceData get() {
		for (int tries = 0; tries < LEVEL0_SEARCH_LIMIT; tries++) {
			long netSeed = randNetSeeds.nextLong();
			Optional<Net> maybeNet = drawNet(netSeed);
			if (maybeNet.isPresent()) {
				Ent ent = new Ent(maybeNet.get());
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
		ValueDrawing drawing = new DefaultValueDrawing();
		RandomNetCreator creator = new RandomNetCreator(rand, drawing);
		creator.setNumberOfNodes(noNodes);
		return creator.drawNetMaybe();
	}

	private void logReject(long seed) {
		if (log.isTraceEnabled()) {
			log.trace("#{} :reject", toHex(seed));
		}
	}

	public int getNoNodes() {
		return noNodes;
	}

	public void setNoNodes(int noNodes) {
		this.noNodes = noNodes;
	}

}
