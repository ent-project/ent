package org.ent.dev;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ent.dev.Level1.NetInfoLevel1;
import org.ent.dev.Level2.NetInfoLevel2;
import org.ent.dev.StepsExam.StepsExamResult;
import org.ent.dev.plan.Data.PropNet;
import org.ent.dev.plan.Data.PropReplicator;
import org.ent.dev.plan.Data.PropSourceInfo;
import org.ent.dev.plan.Data.PropStepsExamResult;
import org.ent.dev.plan.DataImpl;
import org.ent.dev.plan.Processor;
import org.ent.dev.plan.Supplier;
import org.ent.net.Net;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.util.NetCopy;
import org.slf4j.Logger;

public class Level2 implements Processor<NetInfoLevel1, NetInfoLevel2> {

	private static final int LEVEL2_SEARCH_LIMIT = 10_000_000;

	private static final double EXCLUDE_RATE_PRIMARY_SUCCESS = 1;

	private static final double EXCLUDE_RATE_PRIMARY_FAIL = 1. / 10;

	private static final double EXCLUDE_RATE_JOINING_SUCCESS = 1. / 2;

	private static final double EXCLUDE_RATE_JOINING_FAIL = 1. / 50;

	private static final Map<Boolean, Double> EXCLUDE_RATE_PRIMARY = new HashMap<>();

	private static final Map<Boolean, Double> EXCLUDE_RATE_JOINING = new HashMap<>();

	static {
		EXCLUDE_RATE_PRIMARY.put(true, EXCLUDE_RATE_PRIMARY_SUCCESS);
		EXCLUDE_RATE_PRIMARY.put(false, EXCLUDE_RATE_PRIMARY_FAIL);
		EXCLUDE_RATE_JOINING.put(true, EXCLUDE_RATE_JOINING_SUCCESS);
		EXCLUDE_RATE_JOINING.put(false, EXCLUDE_RATE_JOINING_FAIL);
	}

	private static final int POOL_SIZE = 20;

	private static final int LEVEL2_MIN_STEPS = 2;

	private static final int STEP_LIMIT = 6;

	private static final boolean LEVEL2_COMMAND_EXECUTION_FAILED_IS_FATAL = true;

	private static final boolean LEVEL2_INVALID_COMMAND_BRANCH_IS_FATAL = true;

	private static final boolean LEVEL2_INVALID_COMMAND_NODE_IS_FATAL = true;

	private final Random rand;

	private final List<PoolNetInfo> pool;

	private Supplier<NetInfoLevel1> upstream;

	private Level2EventListener listener;

	private NetInfoLevel2 next;

	public interface Level2EventListener {
		void fetchFromPreviousLevel(NetInfoLevel1 netInfo);
		void removeFromPool(NetInfoLevel1 netInfo);
		void directPass(NetInfoLevel1 netInfo);
		void examPassed(StepsExamResult result, Net candidate, NetInfoLevel1 netInfoPrimary, NetInfoLevel1 netInfoJoining);
		void examFailed(StepsExamResult result, Net candidate, NetInfoLevel1 netInfoPrimary, NetInfoLevel1 netInfoJoining);
	}

	private class PoolNetInfo {
		NetInfoLevel1 level1Info;

		public PoolNetInfo(NetInfoLevel1 level1Info) {
			this.level1Info = level1Info;
		}
	}

	public static class NetInfoLevel2 extends DataImpl implements
			PropNet,
			PropStepsExamResult,
			PropReplicator,
			PropSourceInfo {

		public NetInfoLevel2(Net net, StepsExamResult stepsExamResult, String sourceInfo) {
			setNet(net);
			setReplicator(new CopyReplicator(net));
			setStepsExamResult(stepsExamResult);
			setSourceInfo(sourceInfo);
		}

		public NetInfoLevel2(NetInfoLevel1 netInfoLevel1) {
			super(netInfoLevel1.getProperties());
			setSourceInfo("#" + netInfoLevel1.getSerialNumber());
		}

		public void log(Logger log) {
			if (log.isTraceEnabled()) {
				NetFormatter formatter = new NetFormatter();
				log.trace("{} [{}] {}", getSourceInfo(), getStepsExamResult().getSteps(), formatter.format(getNet()));
			}
		}
	}

	public Level2(Random rand) {
		this(null, rand);
	}

	public Level2(Supplier<NetInfoLevel1> upstream, Random rand) {
		this.upstream = upstream;
		this.rand = rand;
		this.pool = new ArrayList<>(POOL_SIZE);
		for (int i = 0; i < POOL_SIZE; i++) {
			this.pool.add(null);
		}
	}

	@Override
	public void setUpstream(Supplier<NetInfoLevel1> upstream) {
		this.upstream = upstream;
	}

	public Level2 withUpstream(Supplier<NetInfoLevel1> upstream) {
		setUpstream(upstream);
		return this;
	}

	@Override
	public Supplier<NetInfoLevel1> getUpstream() {
		return upstream;
	}

	public void setEventListener(Level2EventListener listener) {
		this.listener = listener;
	}

	public Level2 withEventListener(Level2EventListener listener) {
		setEventListener(listener);
		return this;
	}

	@Override
	public NetInfoLevel2 next() {
		findNext();
		return popNext();
	}

	public void findNext() {
		for (int tries = 1; tries <= LEVEL2_SEARCH_LIMIT; tries++) {

			int idxPrimary = rand.nextInt(POOL_SIZE);
			populatePool(idxPrimary);
			if (hasNext()) return;
			PoolNetInfo poolInfoPrimary = pool.get(idxPrimary);

			int idxJoining = rand.nextInt(POOL_SIZE - 1);
			if (idxJoining >= idxPrimary) {
				idxJoining++;
			}
			populatePool(idxJoining);
			if (hasNext()) return;
			PoolNetInfo poolInfoJoining = pool.get(idxJoining);

			Net candidate = produceCandidate(poolInfoPrimary, poolInfoJoining);
			StepsExamResult result = exam(candidate);
			boolean passed = passes(result);
			if (listener != null) {
				if (passed) {
					listener.examPassed(result, candidate, poolInfoPrimary.level1Info, poolInfoJoining.level1Info);
				} else {
					listener.examFailed(result, candidate, poolInfoPrimary.level1Info, poolInfoJoining.level1Info);
				}
			}

			maybeRemoveFromPool(idxPrimary, EXCLUDE_RATE_PRIMARY.get(passed));
			maybeRemoveFromPool(idxJoining, EXCLUDE_RATE_JOINING.get(passed));

			if (passed) {
				pushNext(new NetInfoLevel2(candidate, result, buildSourceInfo(poolInfoPrimary, poolInfoJoining)));
				return;
			}
		}
		throw new RuntimeException("Level2 search limit exceeded (" + LEVEL2_SEARCH_LIMIT + ")");
	}

	private String buildSourceInfo(PoolNetInfo poolInfoPrimary, PoolNetInfo poolInfoJoining) {
		return "(#" + poolInfoPrimary.level1Info.getSerialNumber() + "+#" + poolInfoJoining.level1Info.getSerialNumber() + ")";
	}

	private StepsExamResult exam(Net candidate) {
		StepsExam exam = new StepsExam(getRunSetup());
		NetCopy copy = new NetCopy(candidate);
		return exam.examine(copy.createCopy());
	}

	private void populatePool(int idx) {
		PoolNetInfo poolInfo = pool.get(idx);
		if (poolInfo == null) {
			NetInfoLevel1 nextLevel1 = upstream.next();
			poolInfo = new PoolNetInfo(nextLevel1);
			if (listener != null) {
				listener.fetchFromPreviousLevel(nextLevel1);
			}
			if (passes(nextLevel1.getStepsExamResult())) {
				if (listener != null) {
					listener.directPass(nextLevel1);
				}
				pushNext(new NetInfoLevel2(nextLevel1));
			} else {
				pool.set(idx, poolInfo);
			}
		}
	}

	private void maybeRemoveFromPool(int idx, Double propability) {
		if (rand.nextDouble() < propability) {
			if (listener != null) {
				listener.removeFromPool(pool.get(idx).level1Info);
			}
			pool.set(idx, null);
		}
	}

	private void pushNext(NetInfoLevel2 level2NetInfo) {
		if (hasNext()) {
			throw new AssertionError();
		}
		next = level2NetInfo;
	}

	private NetInfoLevel2 popNext() {
		if (!hasNext()) {
			throw new AssertionError();
		}
		NetInfoLevel2 result = this.next;
		this.next = null;
		return result;
	}

	private boolean hasNext() {
		return next != null;
	}

	private Net produceCandidate(PoolNetInfo infoPrimary, PoolNetInfo infoJoining) {

		Net primaryNetReplica = infoPrimary.level1Info.getReplicator().getNewSpecimen();
		Net joiningNetReplica = infoJoining.level1Info.getReplicator().getNewSpecimen();

		NetMixer mixer = new NetMixer(rand, primaryNetReplica, joiningNetReplica);

		mixer.join();

		return primaryNetReplica;
	}

	private boolean passes(StepsExamResult exam) {
		return passesThreshold(exam.getSteps());
	}

	private boolean passesThreshold(int steps) {
		return steps >= LEVEL2_MIN_STEPS && steps < STEP_LIMIT;
	}

	private RunSetup getRunSetup() {
		return new RunSetup.Builder()
				.withCommandExecutionFailedIsFatal(LEVEL2_COMMAND_EXECUTION_FAILED_IS_FATAL)
				.withInvalidCommandBranchIsFatal(LEVEL2_INVALID_COMMAND_BRANCH_IS_FATAL)
				.withInvalidCommandNodeIsFatal(LEVEL2_INVALID_COMMAND_NODE_IS_FATAL)
				.withMaxSteps(STEP_LIMIT)
				.build();
	}

}
