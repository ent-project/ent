package org.ent.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.junit.jupiter.api.Test;

class ModifiedPoissonTest {

	@Test
	void getCdf() throws Exception {
		double lambda = 5.7;
		PoissonDistribution poissonDistr = new PoissonDistribution(lambda);
		ModifiedPoisson modifiedPoisson = ModifiedPoisson.getModifiedPoisson(lambda);

		for (int k = 0; k <= modifiedPoisson.getCutoff(); k++) {
			double pExpected = poissonDistr.cumulativeProbability(k);
			double pActual = modifiedPoisson.getCdf(k);

			assertThat(pActual).isCloseTo(pExpected, within(1e-8));
		}
	}

	@Test
	void inverseCdfLookup() throws Exception {
		double lambda = 1.2;
		ModifiedPoisson mp = ModifiedPoisson.getModifiedPoisson(lambda);
		PoissonDistribution pd = new PoissonDistribution(lambda);

		double p0 = pd.probability(0);
		double p1 = pd.probability(1);
		double p2 = pd.probability(2);
		double p3 = pd.probability(3);

		double epsilon = 1e-7;

		assertThat(mp.inverseCdfLookup(0)).isZero();
		assertThat(mp.inverseCdfLookup(epsilon)).isZero();
		assertThat(mp.inverseCdfLookup(p0 - epsilon)).isZero();
		assertThat(mp.inverseCdfLookup(p0 + epsilon)).isEqualTo(1);
		assertThat(mp.inverseCdfLookup(p0 + p1 - epsilon)).isEqualTo(1);
		assertThat(mp.inverseCdfLookup(p0 + p1 + epsilon)).isEqualTo(2);
		assertThat(mp.inverseCdfLookup(p0 + p1 + p2 - epsilon)).isEqualTo(2);
		assertThat(mp.inverseCdfLookup(p0 + p1 + p2 + epsilon)).isEqualTo(3);
		assertThat(mp.inverseCdfLookup(p0 + p1 + p2 + p3 - epsilon)).isEqualTo(3);
		assertThat(mp.inverseCdfLookup(p0 + p1 + p2 + p3 + epsilon)).isEqualTo(4);
	}

	@Test
	void getCutoffValue() throws Exception {
		assertThat(ModifiedPoisson.getModifiedPoisson(0.5).getCutoffValue()).isEqualTo(21);
		assertThat(ModifiedPoisson.getModifiedPoisson(6.7).getCutoffValue()).isEqualTo(21);
		assertThat(ModifiedPoisson.getModifiedPoisson(7.9).getCutoffValue()).isEqualTo(24);
		assertThat(ModifiedPoisson.getModifiedPoisson(100.0).getCutoffValue()).isEqualTo(300);
	}

}
