package org.ent.dev.randnet;

import org.ent.net.Net;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThatCode;

class RandomNetCreatorTest {

	private static final int NO_DRAWS = 100;

	private RandomNetCreator netCreator;

	@BeforeEach
	void setUpRandomNetCreator() {
		Random randMaster = RandomTestUtil.newRandom();
		Random randCommandDrawing = new Random(randMaster.nextLong());
		Random randNetCreator = new Random(randMaster.nextLong());
		ValueDrawing commandDrawing = new DefaultValueDrawing();
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

}
