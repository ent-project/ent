package org.ent.dev.plan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ent.dev.NetMixer;
import org.ent.dev.plan.DataProperties.PropNet;
import org.ent.dev.plan.DataProperties.PropReplicator;
import org.ent.dev.unit.Dan;
import org.ent.dev.unit.Req;
import org.ent.dev.unit.Sup;
import org.ent.dev.unit.data.Data;
import org.ent.dev.unit.data.DataImpl;
import org.ent.dev.unit.data.DataProxy;
import org.ent.dev.unit.local.Filter;
import org.ent.net.Net;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pool {

	private static final Logger log = LoggerFactory.getLogger(Pool.class);

	private static final int POOL_SIZE = 20;

	private double excludeRatePrimarySuccess = 1;

	private double excludeRatePrimaryFail = 1. / 10;

	private double excludeRateJoiningSuccess = 1. / 2;

	private double excludeRateJoiningFail = 1. / 50;

	private double rewireFraction = 0.2;

	private final Map<Boolean, Double> excludeRatePrimary = new HashMap<>();

	private final Map<Boolean, Double> excludeRateJoining = new HashMap<>();

	private final List<PoolData> pool;

	private Sup upstream;

	private Req downstream;

	private final Random rand;

	private State state;

	private Integer idxPrimary;

	private Integer idxJoining;

	private final PoolProc poolProc;

	private enum State {
		START, WAITING_FOR_PRIMARY, WAITING_FOR_JOINING, ACCEPTING_FEEDBACK;
	}

	private class PoolProc implements Dan {

		@Override
		public void setUpstream(Sup upstream) {
			Pool.this.upstream = upstream;
		}

		@Override
		public void setDownstream(Req downstream) {
			Pool.this.downstream = downstream;
		}

		@Override
		public void requestNext() {
			Pool.this.requestNext();

		}

		@Override
		public void receiveNext(Data next) {
			Pool.this.receiveNext(next);
		}
	}

	private class PoolData extends DataProxy implements PropReplicator{
		public PoolData(Data data) {
			super(data);
		}
	}

	private static class PoolOutputDataImpl extends DataImpl implements PropNet {}

	private class PoolFeedback implements Filter {

		private Filter delegate;

		public PoolFeedback(Filter delegate) {
			this.delegate = delegate;
		}

		@Override
		public boolean test(Data element) {
			log.trace("PoolFeedback: passes?");
			boolean passed = delegate.test(element);
			log.trace("PoolFeedback: delegate:" + passed);

			if (state != State.ACCEPTING_FEEDBACK) {
				throw new AssertionError("Providing feedback to pool, but is not in state to accept feedback; current state=" + state);
			}
			maybeRemoveFromPool(idxPrimary, excludeRatePrimary.get(passed));
			maybeRemoveFromPool(idxJoining, excludeRateJoining.get(passed));

			reset();
			return passed;
		}
	}

	public Pool(Random rand) {
		this.rand = rand;
		this.pool = new ArrayList<>(POOL_SIZE);
		for (int i = 0; i < POOL_SIZE; i++) {
			this.pool.add(null);
		}
		this.poolProc = new PoolProc();
		initializeRateMaps();
		reset();
	}

	public double getExcludeRatePrimarySuccess() {
		return excludeRatePrimarySuccess;
	}

	public void setExcludeRatePrimarySuccess(double excludeRatePrimarySuccess) {
		this.excludeRatePrimarySuccess = excludeRatePrimarySuccess;
		initializeRateMaps();
	}

	public double getExcludeRatePrimaryFail() {
		return excludeRatePrimaryFail;
	}

	public void setExcludeRatePrimaryFail(double excludeRatePrimaryFail) {
		this.excludeRatePrimaryFail = excludeRatePrimaryFail;
		initializeRateMaps();
	}

	public double getExcludeRateJoiningSuccess() {
		return excludeRateJoiningSuccess;
	}

	public void setExcludeRateJoiningSuccess(double excludeRateJoiningSuccess) {
		this.excludeRateJoiningSuccess = excludeRateJoiningSuccess;
		initializeRateMaps();
	}

	public double getExcludeRateJoiningFail() {
		return excludeRateJoiningFail;
	}

	public void setExcludeRateJoiningFail(double excludeRateJoiningFail) {
		this.excludeRateJoiningFail = excludeRateJoiningFail;
		initializeRateMaps();
	}

	public double getRewireFraction() {
		return rewireFraction;
	}

	public void setRewireFraction(double rewireFraction) {
		this.rewireFraction = rewireFraction;
	}

	private void maybeRemoveFromPool(int idx, Double propability) {
		if (rand.nextDouble() < propability) {
			pool.set(idx, null);
		}
	}

	public Dan withFeedback(Filter filter) {
		return poolProc.combineFilter(new PoolFeedback(filter));
	}

	private void requestNext() {
		log.trace("entering requestNext");
		switch (state) {
		case START:
			stage1();
			break;
		case WAITING_FOR_PRIMARY:
		case WAITING_FOR_JOINING:
			upstream.requestNext();
			break;
		default:
			throw new AssertionError();
		}
	}

	private void receiveNext(Data next) {
		log.trace("entering receiveNext");
		switch (state) {
		case WAITING_FOR_PRIMARY:
			pool.set(idxPrimary, new PoolData(next));
			stage2();
			break;
		case WAITING_FOR_JOINING:
			pool.set(idxJoining, new PoolData(next));
			stage3();
			break;
		default:
			throw new AssertionError("Not waiting for next from upstream");
		}
	}

	private void stage1() {
		log.trace("entering stage1");
		idxPrimary = rand.nextInt(POOL_SIZE);
		Data dataPrimary = pool.get(idxPrimary);
		if (dataPrimary == null) {
			state = State.WAITING_FOR_PRIMARY;
			upstream.requestNext();
		} else {
			stage2();
		}
	}

	private void stage2() {
		log.trace("entering stage2");
		idxJoining = rand.nextInt(POOL_SIZE - 1);
		if (idxJoining >= idxPrimary) {
			idxJoining++;
		}
		Data dataJoining = pool.get(idxJoining);
		if (dataJoining == null) {
			state = State.WAITING_FOR_JOINING;
			upstream.requestNext();
		} else {
			stage3();
		}
	}

	private void stage3() {
		log.trace("entering stage3");
		PoolData poolDataPrimary = pool.get(idxPrimary);
		PoolData poolDataJoining = pool.get(idxJoining);

		Net offspring = produceOffspring(poolDataPrimary, poolDataJoining);
		PropNet offspringData = new PoolOutputDataImpl();
		offspringData.setNet(offspring);
		state = State.ACCEPTING_FEEDBACK;
		downstream.deliver(offspringData);
	}

	private Net produceOffspring(PoolData infoPrimary, PoolData infoJoining) {

		Net primaryNetReplica = infoPrimary.getReplicator().getNewSpecimen();
		Net joiningNetReplica = infoJoining.getReplicator().getNewSpecimen();

		NetMixer mixer = new NetMixer(rand, primaryNetReplica, joiningNetReplica);
		mixer.setRewireFraction(rewireFraction);

		mixer.join();

		return primaryNetReplica;
	}

	private void reset() {
		state = State.START;
		idxPrimary = null;
		idxJoining = null;
	}

	private void initializeRateMaps() {
		excludeRatePrimary.put(true, excludeRatePrimarySuccess);
		excludeRatePrimary.put(false, excludeRatePrimaryFail);
		excludeRateJoining.put(true, excludeRateJoiningSuccess);
		excludeRateJoining.put(false, excludeRateJoiningFail);
	}
}
