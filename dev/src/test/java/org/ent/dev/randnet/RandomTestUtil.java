package org.ent.dev.randnet;

import org.ent.net.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

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
		return RandomUtil.newRandom(seed);
	}
}
