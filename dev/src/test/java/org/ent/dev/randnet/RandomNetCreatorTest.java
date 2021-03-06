package org.ent.dev.randnet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

import org.assertj.core.api.Assertions;
import org.ent.net.Net;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.CommandFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RandomNetCreatorTest {

	private static final int NO_DRAWS = 100;

	private RandomNetCreator netCreator;

	@BeforeEach
	void setUpRandomNetCreator() {
		Random randMaster = RandomTestUtil.newRandom();
		Random randCommandDrawing = new Random(randMaster.nextLong());
		Random randNetCreator = new Random(randMaster.nextLong());
		CommandDrawingImpl commandDrawing = buildCommandDrawing(randCommandDrawing);
		netCreator = new RandomNetCreator(randNetCreator, commandDrawing);
	}

	@Test
	void drawNet() {
		for (int i = 0; i < NO_DRAWS; i++) {
			Optional<Net> drawnNet = netCreator.drawNet();

			assertThatCode(() -> drawnNet.ifPresent(Net::consistencyCheck))
					.doesNotThrowAnyException();
		}
	}

	@Test
	void drawNet_noBNodes() {
		netCreator.setFractionBNodes(0.0);
		netCreator.setFractionCNodes(0.5);
		netCreator.setFractionUNodes(0.5);

		for (int i = 0; i < NO_DRAWS; i++) {
			Optional<Net> drawnNet = netCreator.drawNet();

			assertThat(drawnNet).isNotPresent();
		}
	}

	private CommandDrawingImpl buildCommandDrawing(Random rand) {
		Command nopCommand = CommandFactory.createNopCommand();
		Command ixCommand = CommandFactory.createAncestorSwapCommand();
		Command evalCommand = CommandFactory.createEvalCommand(0);
		return new CommandDrawingImpl(rand,
				Arrays.asList(
						new CommandCandidate(nopCommand, 1.0),
						new CommandCandidate(ixCommand, 5.0),
						new CommandCandidate(evalCommand, 3.0)));
	}

}
