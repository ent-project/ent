package org.ent.dev.unit;

/**
 * A Dan is one stage of the plan.
 *
 * It requests elements from upstream and delivers elements downstream.
 *
 * A dan gets active, when it gets a request from downstream.
 * To produce output, it may in turn request elements from upstream.
 * Eventually it shall produce an element and deliver that output downstream.
 *
 * A request to upstream is not necessarily answered. In this case, the
 * dan is supposed save its state and "pause".
 * Upon another request from downstream, it shall repeat its request to upstream.
 * This goes on, until upstream finally responds. Then, the dan proceeds with its operation.
 *
 * You must not deliver an element to a dan, unless it has requested an element beforehand.
 * It must be ensured, that each request results in at most one delivery.
 *
 * The simplest case is to wire dans in a chain, i.e. dan1 is upstream of dan2 and
 * dan2 is downstream of dan1. However, the wiring can be arbitrary,
 * as long as the general expectations are met for each dan.
 */
public interface Dan extends Sup, Req {

	@Override
	default Dan combine(Dan downstream) {
		return new DanDan(this, downstream);
	}

	@Override
	default Dan combinePipe(Pipe pipe) {
		return combine(new PipeDan(pipe));
	}

	@Override
	default Dan combineFilter(Filter filter) {
		return combine(new FilterDan(filter));
	}

}
