package org.ent.dev.randnet;

import org.ent.net.ArrowDirection;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.CommandFactory;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class CommandDrawingImplTest {

	@Test
	void randomValueToIndex() {
		Command nopCommand = CommandFactory.createNopCommand();
		CommandDrawingImpl commandDrawing = new CommandDrawingImpl(RandomTestUtil.newRandom(),
				Arrays.asList(
						new CommandCandidate(nopCommand, 2.0),
						new CommandCandidate(nopCommand, 5.0),
						new CommandCandidate(nopCommand, 10.0)));

		assertThat(commandDrawing.randomValueToIndex(0.0)).isZero();
		assertThat(commandDrawing.randomValueToIndex(0.1)).isZero();
		assertThat(commandDrawing.randomValueToIndex(1.9)).isZero();
		assertThat(commandDrawing.randomValueToIndex(2.0)).isZero();
		assertThat(commandDrawing.randomValueToIndex(2.1)).isEqualTo(1);
		assertThat(commandDrawing.randomValueToIndex(6.9)).isEqualTo(1);
		assertThat(commandDrawing.randomValueToIndex(7.0)).isEqualTo(1);
		assertThat(commandDrawing.randomValueToIndex(7.1)).isEqualTo(2);
		assertThat(commandDrawing.randomValueToIndex(16.9)).isEqualTo(2);
		assertThat(commandDrawing.randomValueToIndex(17.0)).isEqualTo(2);
		assertThat(commandDrawing.randomValueToIndex(17.01)).isEqualTo(2);
	}

	@Test
	void drawCommand() throws Exception {
		Command nopCommand = CommandFactory.createNopCommand();
		Command ixCommand = CommandFactory.createAncestorSwapCommand();
		Command setCommand = CommandFactory.createSetCommandL(ArrowDirection.LEFT);
		CommandDrawingImpl commandDrawing = new CommandDrawingImpl(RandomTestUtil.newRandom(),
				Arrays.asList(
						new CommandCandidate(nopCommand, 1.0),
						new CommandCandidate(ixCommand, 5.0),
						new CommandCandidate(setCommand, 3.0)));

		for (int i = 0; i < 100; i++) {
			Command drawnCommand = commandDrawing.drawCommand();

			assertThat(drawnCommand).isIn(nopCommand, ixCommand, setCommand);
		}
	}



}
