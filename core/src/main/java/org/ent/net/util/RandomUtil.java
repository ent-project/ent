package org.ent.net.util;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.JDKRandomBridge;
import org.apache.commons.rng.simple.RandomSource;

import java.util.Random;

public class RandomUtil {

    private RandomUtil() {
    }

    public static Random newRandom(long seed) {
        return new JDKRandomBridge(RandomSource.PCG_MCG_XSH_RR_32, seed);
    }

    public static UniformRandomProvider newRandom2(long seed) {
        return RandomSource.PCG_MCG_XSH_RR_32.create(seed);
    }
}
