package org.ent.net.util;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;

public class RandomUtil {

    private static final HashFunction MURMUR_128 = Hashing.murmur3_128(0b10010100101101001101111010111001);

    private RandomUtil() {
    }

    public static UniformRandomProvider newRandom(long seed) {
        return newRandomNoScramble(scrambleSeed(seed));
    }

    public static UniformRandomProvider newRandom(UniformRandomProvider seedProvider) {
        return newRandomNoScramble(seedProvider.nextLong());
    }

    /**
     * Make sure to provide a good seed. Essentially it must be a random-looking bit-pattern
     * over all the bits in a {@code long}.
     * Any hand-picked value, such as 12345L will not work. (I.e. at least the first draw will have a
     * predictable bit pattern.)
     *
     * Use {@link RandomUtil#newRandom(long)} for hand-picked seeds.
     */
    public static UniformRandomProvider newRandomNoScramble(long seed) {
        return RandomSource.PCG_MCG_XSH_RR_32.create(seed);
    }

    public static long scrambleSeed(long seed) {
        return MURMUR_128.newHasher().putLong(seed).hash().asLong();
    }

}
