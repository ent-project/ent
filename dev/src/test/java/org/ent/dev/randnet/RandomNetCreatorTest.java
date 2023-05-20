package org.ent.dev.randnet;

import org.ent.net.Net;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.CommandFactory;
import org.ent.net.node.cmd.Commands;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.ent.net.node.cmd.accessor.Accessors.DIRECT;
import static org.ent.net.node.cmd.accessor.Accessors.LEFT;
import static org.ent.net.node.cmd.operation.Operations.SET_OPERATION;

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

	private CommandDrawingImpl buildCommandDrawing(Random rand) {
		Command nopCommand = CommandFactory.createNopCommand();
		Command ixCommand = CommandFactory.createAncestorSwapCommand();
		Command setCommand = Commands.get(LEFT, SET_OPERATION, DIRECT);
		return new CommandDrawingImpl(rand,
				Arrays.asList(
						new CommandCandidate(nopCommand, 1.0),
						new CommandCandidate(ixCommand, 5.0),
						new CommandCandidate(setCommand, 3.0)));
	}

}
