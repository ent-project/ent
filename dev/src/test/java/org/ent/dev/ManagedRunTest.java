package org.ent.dev;

import org.ent.net.io.formatter.NetFormatter;
import org.ent.run.NetRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ent.run.StepResult.COMMAND_EXECUTION_FAILED;
import static org.ent.run.StepResult.FATAL;
import static org.ent.run.StepResult.SUCCESS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ManagedRunTest {

	private static final int MAX_STEPS = 5;

	@Mock
	NetRunner netRunner;

	@Mock
	NetFormatter netFormatter;

	@Test
	void perform_okay() {
		Mockito.doReturn(SUCCESS).when(netRunner).step();
		ManagedRun run = new ManagedRun(RunSetup.create(s -> s.withMaxSteps(MAX_STEPS)))
                .withNetRunner(netRunner).withFormatter(netFormatter);

		run.perform();

		verify(netRunner, times(MAX_STEPS)).step();
		assertThat(run.getNoSteps()).isEqualTo(MAX_STEPS);
	}

	@Test
	void perform_stop_fatal() {
		doReturn(FATAL).when(netRunner).step();
		ManagedRun run = new ManagedRun(RunSetup.create(s -> s)).withNetRunner(netRunner).withFormatter(netFormatter);

		run.perform();

		verify(netRunner, times(1)).step();
		assertThat(run.getNoSteps()).isZero();
	}

	@Test
	void perform_okay_commandExecutionFailedIsNotFatal() {
		doReturn(SUCCESS, SUCCESS, COMMAND_EXECUTION_FAILED).when(netRunner).step();
		ManagedRun run = new ManagedRun(
		            RunSetup.create(s -> s
                        .withCommandExecutionFailedIsFatal(false)
                        .withMaxSteps(MAX_STEPS)))
                .withNetRunner(netRunner)
                .withFormatter(netFormatter);

		run.perform();

		verify(netRunner, times(MAX_STEPS)).step();
		assertThat(run.getNoSteps()).isEqualTo(MAX_STEPS);
	}

	@Test
	void perform_stop_commandExecutionFailed() {
		doReturn(SUCCESS, SUCCESS, COMMAND_EXECUTION_FAILED).when(netRunner).step();
		ManagedRun run = new ManagedRun(
                    RunSetup.create(s -> s
                        .withCommandExecutionFailedIsFatal(true)
                        .withMaxSteps(MAX_STEPS)))
                .withNetRunner(netRunner)
                .withFormatter(netFormatter);

		run.perform();

		verify(netRunner, times(3)).step();
		assertThat(run.getNoSteps()).isEqualTo(2);
	}

}
