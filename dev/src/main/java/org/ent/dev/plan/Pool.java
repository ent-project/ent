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
import org.ent.dev.unit.Data;
import org.ent.dev.unit.DataImpl;
import org.ent.dev.unit.DataProxy;
import org.ent.dev.unit.Filter;
import org.ent.dev.unit.Req;
import org.ent.dev.unit.Sup;
import org.ent.net.Net;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pool {

	private static final Logger log = LoggerFactory.getLogger(Pool.class);

	private static final int POOL_SIZE = 20;

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
			maybeRemoveFromPool(idxPrimary, EXCLUDE_RATE_PRIMARY.get(passed));
			maybeRemoveFromPool(idxJoining, EXCLUDE_RATE_JOINING.get(passed));

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
		reset();
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

		mixer.join();

		return primaryNetReplica;
	}

	private void reset() {
		state = State.START;
		idxPrimary = null;
		idxJoining = null;
	}

}
