package org.ent.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Modified version of the Poisson distribution.
 *
 * The probability mass function is cut off at large sample values.
 *
 * For input parameter lambda=10, it returns sample values from the
 * set {0,1,...,30}, but it will never draw values higher than the cutoff.
 *
 * The value of the cutoff is 3 times the Poisson mean value lambda
 * but at least 21.
 *
 * Implementation uses inverse transform sampling.
 */
public class ModifiedPoisson {

	private static final Map<Double, ModifiedPoisson> cache = new HashMap<>();

	private final double lambda;

	private final int cutoff;

	private final double[] cdf;

	private final double maxP;

	public static ModifiedPoisson getModifiedPoisson(double lambda) {
		ModifiedPoisson result = cache.get(lambda);
		if (result == null) {
			result = new ModifiedPoisson(lambda);
			cache.put(lambda, result);
		}
		return result;
	}

    private ModifiedPoisson(double lambda) {
		this.lambda = lambda;
		this.cutoff = getCutoffValue();
		this.cdf = buildCdf();
		this.maxP = cdf[cutoff];
	}

    int getCutoffValue() {
		return Math.max(21, (int) Math.ceil(3 * lambda));
	}

	private double[] buildCdf() {
		double[] cdf = new double[cutoff + 1];
		final double normalizer = Math.exp(-lambda);
		cdf[0] = normalizer;
		double p = 1.0;
		for (int i = 1; i <= cutoff; i++) {
		    p *= lambda / i;
		    cdf[i] = cdf[i - 1] + p * normalizer;
		}
		return cdf;
	}

    public int drawModifiedPoisson(Random rand) {
        double p = rand.nextDouble() * maxP;
        return inverseCdfLookup(p);
    }

	int inverseCdfLookup(double p) {
		return Tools.binarySearchLowerEqual(cdf, p, true);
	}

	double getCdf(int k) {
		if (k < 0) {
			throw new IllegalArgumentException("Input value k must not be negative, but was " + k);
		}
		if (k > cutoff) {
			throw new IllegalArgumentException("Input value k must not exceed cutoff " + cutoff + ", but was " + k);
		}
		return cdf[k];
	}

	int getCutoff() {
		return cutoff;
	}

}
