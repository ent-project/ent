package org.ent.dev.randnet;

import org.apache.commons.rng.RestorableUniformRandomProvider;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.ent.net.Net;
import org.ent.net.util.RandomUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatCode;

class RandomNetCreatorTest {
	private static final Logger log = LoggerFactory.getLogger(RandomNetCreatorTest.class);

	private static final int NO_DRAWS = 100;

	private RandomNetCreator netCreator;

	@BeforeEach
	void setUpRandomNetCreator() {
		UniformRandomProvider randNetCreator = RandomUtil.newRandom(123L);
		DefaultValueDrawing commandDrawing = new DefaultValueDrawing();
		netCreator = new RandomNetCreator(15, randNetCreator, commandDrawing);
	}

	@Test
	void drawNet() {
		for (int i = 0; i < NO_DRAWS; i++) {
			Optional<Net> drawnNet = netCreator.drawNetMaybe();

			assertThatCode(() -> drawnNet.ifPresent(Net::consistencyCheck))
					.doesNotThrowAnyException();
		}
	}

	@Test
	void test() {
		RestorableUniformRandomProvider random = RandomSource.XO_RO_SHI_RO_128_PLUS.create(new long[]{0xadf345ad5aL, 0x1324325L});
		IntStream valueDrawSource = random.ints(5, 19);
		PrimitiveIterator.OfInt iterator = valueDrawSource.iterator();
		int i = iterator.nextInt();
		System.err.println(i);
	}
	@Test
	void testJdk() {
		Random jdkRand = new Random();

		long startTime = System.nanoTime();
		int sum = 0;
		for (int i = 0; i < 500_000_000; i++) {
			sum += jdkRand.nextInt(116);
		}
		Duration duration = Duration.ofNanos(System.nanoTime() - startTime);
		log.info("TOTAL DURATION: {}", duration);
		log.info("sum: {}", sum);
	}

	@Test
	void testApache() {
//		UniformRandomProvider rng = RandomSource.XO_RO_SHI_RO_128_PP.create();
        UniformRandomProvider rng = RandomSource.XO_RO_SHI_RO_128_PLUS.create(new long[] {0xadf345ad5aL, 0x1324325L});
		PrimitiveIterator.OfInt iterator = rng.ints(0, 116).iterator();

		long startTime = System.nanoTime();
		int sum = 0;
		for (int i = 0; i < 500_000_000; i++) {
//			sum += rng.nextInt(0, 116);
			sum += iterator.nextInt();
		}
		Duration duration = Duration.ofNanos(System.nanoTime() - startTime);
		log.info("TOTAL DURATION: {}", duration);
		log.info("sum: {}", sum);
	}
}
