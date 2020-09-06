package org.ent.dev;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ent.run.StepResult.COMMAND_EXECUTION_FAILED;
import static org.ent.run.StepResult.FATAL;
import static org.ent.run.StepResult.SUCCESS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.ent.run.NetRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ManagedRunTest {

	private static final int MAX_STEPS = 5;

	@Mock
	RunSetup setup;

	@Mock
	NetRunner netRunner;

	@Test
	void perform_okay() {
		doReturn(SUCCESS).when(netRunner).step();
		doReturn(MAX_STEPS).when(setup).getMaxSteps();
		ManagedRun run = new ManagedRun(setup).withNetRunner(netRunner);

		run.perform();

		verify(netRunner, times(MAX_STEPS)).step();
		assertThat(run.getNoSteps()).isEqualTo(MAX_STEPS);
	}

	@Test
	void perform_stop_fatal() {
		doReturn(FATAL).when(netRunner).step();
		ManagedRun run = new ManagedRun(setup).withNetRunner(netRunner);

		run.perform();

		verify(netRunner, times(1)).step();
		assertThat(run.getNoSteps()).isZero();
	}

	@Test
	void perform_okay_commandExecutionFailedIsNotFatal() {
		doReturn(SUCCESS, SUCCESS, COMMAND_EXECUTION_FAILED).when(netRunner).step();
		doReturn(false).when(setup).isCommandExecutionFailedIsFatal();
		doReturn(MAX_STEPS).when(setup).getMaxSteps();
		ManagedRun run = new ManagedRun(setup).withNetRunner(netRunner);

		run.perform();

		verify(netRunner, times(MAX_STEPS)).step();
		assertThat(run.getNoSteps()).isEqualTo(MAX_STEPS);
	}

	@Test
	void perform_stop_commandExecutionFailed() {
		doReturn(SUCCESS, SUCCESS, COMMAND_EXECUTION_FAILED).when(netRunner).step();
		doReturn(MAX_STEPS).when(setup).getMaxSteps();
		doReturn(true).when(setup).isCommandExecutionFailedIsFatal();
		ManagedRun run = new ManagedRun(setup).withNetRunner(netRunner);

		run.perform();

		verify(netRunner, times(3)).step();
		assertThat(run.getNoSteps()).isEqualTo(2);
	}

}
