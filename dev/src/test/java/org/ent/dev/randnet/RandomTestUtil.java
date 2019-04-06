package org.ent.dev.randnet;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomTestUtil {

	private static final Logger log = LoggerFactory.getLogger(RandomTestUtil.class);

	private static final long seed;

	static {
		seed = System.currentTimeMillis();
		log.info("Test seed initialized to '{}'", seed);
	}

	public static long getGlobalTestSeed() {
		return seed;
	}

	public static Random newRandom() {
		return new Random(seed);
	}
}
