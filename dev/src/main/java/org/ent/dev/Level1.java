package org.ent.dev;

import static org.ent.net.io.HexConverter.toHex;

import org.ent.dev.Level0.NetInfoLevel0;
import org.ent.net.Net;
import org.ent.net.io.HexConverter;
import org.ent.net.io.formatter.NetFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Level1 {

	private static final Logger log = LoggerFactory.getLogger(Level1.class);
	private static final Logger logReject = LoggerFactory.getLogger(Level1.class.getName() + ".reject");

	private static final int LEVEL1_MIN_STEPS = 1;

	private static final int STEP_LIMIT = 6;

	private static final boolean LEVEL1_COMMAND_EXECUTION_FAILED_IS_FATAL = true;

	private static final boolean LEVEL1_INVALID_COMMAND_BRANCH_IS_FATAL = true;

	private static final boolean LEVEL1_INVALID_COMMAND_NODE_IS_FATAL = true;

	private static final int LEVEL1_SEARCH_LIMIT = 100_000_000;

	private final Level0 level0;

	private Level1EventListener listener;

	private class ExamResult {
		boolean passed;
		int steps;
	}

	public interface Level1EventListener {
		void netExam(int steps, boolean passed, NetInfoLevel0 net0);
	}

	public class NetInfoLevel1  {
		NetInfoLevel0 net0;

		int steps;

		public NetInfoLevel1(NetInfoLevel0 net0) {
			this.net0 = net0;
		}

		public void log() {
			if (log.isTraceEnabled()) {
				NetFormatter formatter = new NetFormatter();
				log.trace("#{} [{}] {}", toHex(net0.getSeed()), steps, formatter.format(net0.getNet()));
			}
		}
	}

	public Level1(Level0 level0) {
		this.level0 = level0;
	}

	public Level1EventListener getLevel1EventListener() {
		return listener;
	}

	public void setLevel1EventListener(Level1EventListener listener) {
		this.listener = listener;
	}

	public NetInfoLevel1 next() {
		for (int tries = 1; tries < LEVEL1_SEARCH_LIMIT; tries++) {
			NetInfoLevel0 candidate = level0.next();
			ExamResult examResult = examine(candidate);
			if (examResult.passed) {
				NetInfoLevel1 graduate = new NetInfoLevel1(candidate);
				graduate.steps = examResult.steps;
				graduate.log();
				return graduate;
			} else {
				if (log.isTraceEnabled()) {
					logReject.trace("#{} :reject", HexConverter.toHex(candidate.getSeed()));
				}
			}
		}
		throw new RuntimeException("Level1 search limit exceeded (" + LEVEL1_SEARCH_LIMIT + ")");
	}

	public ExamResult examine(NetInfoLevel0 candidate) {
		Net net = candidate.getNewSpecimen();
		RunSetup setup = getRunSetup();
		ManagedRun run = new ManagedRun(setup).withNet(net);
		run.perform();
		int steps = run.getNoSteps();
		ExamResult result = new ExamResult();
		result.steps = steps;
		result.passed = passesThreshold(steps);
		if (listener != null) {
			listener.netExam(result.steps, result.passed, candidate);
		}
		return result;
	}

	private RunSetup getRunSetup() {
		return new RunSetup.Builder()
				.withCommandExecutionFailedIsFatal(LEVEL1_COMMAND_EXECUTION_FAILED_IS_FATAL)
				.withInvalidCommandBranchIsFatal(LEVEL1_INVALID_COMMAND_BRANCH_IS_FATAL)
				.withInvalidCommandNodeIsFatal(LEVEL1_INVALID_COMMAND_NODE_IS_FATAL)
				.withMaxSteps(STEP_LIMIT)
				.build();
	}

	private boolean passesThreshold(int steps) {
		return steps >= LEVEL1_MIN_STEPS && steps < STEP_LIMIT;
	}

}
