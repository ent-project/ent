package org.ent.dev;

import static org.ent.net.io.HexConverter.toHex;

import org.ent.dev.Level0.NetInfoLevel0;
import org.ent.dev.Level1.NetInfoLevel1;
import org.ent.dev.StepsExam.StepsExamResult;
import org.ent.dev.plan.NetInfo;
import org.ent.dev.plan.Processor;
import org.ent.dev.plan.Supplier;
import org.ent.net.Net;
import org.ent.net.io.HexConverter;
import org.ent.net.io.formatter.NetFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Level1 implements Processor<NetInfoLevel0, NetInfoLevel1> {

	private static final Logger log = LoggerFactory.getLogger(Level1.class);
	private static final Logger logReject = LoggerFactory.getLogger(Level1.class.getName() + ".reject");

	private static final int LEVEL1_MIN_STEPS = 1;

	private static final int STEP_LIMIT = 6;

	private static final boolean LEVEL1_COMMAND_EXECUTION_FAILED_IS_FATAL = true;

	private static final boolean LEVEL1_INVALID_COMMAND_BRANCH_IS_FATAL = true;

	private static final boolean LEVEL1_INVALID_COMMAND_NODE_IS_FATAL = true;

	private static final int LEVEL1_SEARCH_LIMIT = 100_000_000;

	private Supplier<NetInfoLevel0> upstream;

	private Level1EventListener listener;

	private int serialNumberCounter;

	public interface Level1EventListener {
		void netExam(int steps, boolean passed, NetInfoLevel0 net0);
	}

	public class NetInfoLevel1 implements NetInfo {

		private final Net net;

		private final long seed;

		private long serialNumber;

		private final NetReplicator replicator;

		private StepsExamResult stepsExamResult;

		public NetInfoLevel1(NetInfoLevel0 netInfo0) {
			this.net = netInfo0.getNet();
			this.seed = netInfo0.getSeed();
			this.replicator = netInfo0.getReplicator();
			serialNumberCounter++;
			this.serialNumber = serialNumberCounter;
		}

		@Override
		public Net getNet() {
			return net;
		}

		public NetReplicator getReplicator() {
			return replicator;
		}

		public StepsExamResult getStepExamResult() {
			return stepsExamResult;
		}

		public long getSeed() {
			return seed;
		}

		public void log(Logger logger, String prefix) {
			if (logger.isTraceEnabled()) {
				NetFormatter formatter = new NetFormatter();
				logger.trace("{}#{} 0x{} [{}] {}",
						prefix, serialNumber, toHex(seed), stepsExamResult.getSteps(), formatter.format(net));
			}
		}

		public void log(Logger logger) {
			log(logger, "");
		}

		public long getSerialNumber() {
			return serialNumber;
		}

		public void setSerialNumber(long serialNumber) {
			this.serialNumber = serialNumber;

		}
	}

	public Level1() {
	}

	public Level1(Level0 level0) {
		this.upstream = level0;
	}

	@Override
	public void setUpstream(Supplier<NetInfoLevel0> upstream) {
		this.upstream = upstream;
	}

	public Level1 withUpstream(Supplier<NetInfoLevel0> upstream) {
		setUpstream(upstream);
		return this;
	}

	@Override
	public Supplier<NetInfoLevel0> getUpstream() {
		return upstream;
	}

	public Level1EventListener getLevel1EventListener() {
		return listener;
	}

	public void setLevel1EventListener(Level1EventListener listener) {
		this.listener = listener;
	}

	public Level1 withLevel1EventListener(Level1EventListener listener) {
		setLevel1EventListener(listener);
		return this;
	}

	@Override
	public NetInfoLevel1 next() {
		for (int tries = 1; tries < LEVEL1_SEARCH_LIMIT; tries++) {
			NetInfoLevel0 candidate = upstream.next();

			StepsExam exam = new StepsExam(getRunSetup());
			Net netExamSpecimen = candidate.getReplicator().getNewSpecimen();
			StepsExamResult result = exam.examine(netExamSpecimen);
			boolean passed = passes(result);
			if (listener != null) {
				listener.netExam(result.getSteps(), passed, candidate);
			}
			if (passed) {
				NetInfoLevel1 graduate = new NetInfoLevel1(candidate);
				graduate.stepsExamResult = result;
				graduate.log(log);
				return graduate;
			} else {
				if (log.isTraceEnabled()) {
					logReject.trace("#{} :reject", HexConverter.toHex(candidate.getSeed()));
				}
			}
		}
		throw new RuntimeException("Level1 search limit exceeded (" + LEVEL1_SEARCH_LIMIT + ")");
	}

	private boolean passes(StepsExamResult result) {
		return passesThreshold(result.getSteps());
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
