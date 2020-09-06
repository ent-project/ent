package org.ent.dev.unit;

import org.ent.dev.unit.data.Data;
import org.ent.dev.unit.local.Filter;
import org.ent.dev.unit.local.Pipe;

/**
 * Splits upstream data into two lanes, according to a sorter.
 *
 * The two lanes are called light lane (a Pipe) and heavy lane (a Dan).
 * Requests to the splitter are forwarded to the heavy lane.
 *
 * The heavy lane may require zero, one or more input elements to produce one output element
 * (which will then be delivered downstream).
 * Requests from the heavy lane go upstream. However, upstream does not deliver
 * directly to the heavy lane, but to the splitter.
 *
 * The splitter applies the sorter. Data that passes the sorter will be processed by the
 * light lane and delivered directly downstream.
 * Data that does not pass, will be delivered to the heavy lane.
 *
 * From the heavy lane's perspective, a requests does not necessarily trigger a
 * response. The request may be silently ignored (i.e. when the element from upstream passes
 * the sorter). The heavy lane must be prepared to "pause" at this point and wait for
 * another request from downstream.
 * It shall then repeat its request to upstream and carry on in case it gets a response
 * this time.
 */
public class SkewSplitter implements Dan {

	private Filter sorter;

	private Pipe lightLane;

	private Dan heavyLane;

	private Req downstream;

	public void setHeavyLane(Dan heavyLane) {
		this.heavyLane = heavyLane;
	}

	public SkewSplitter withHeavyLane(Dan heavyLane) {
		setHeavyLane(heavyLane);
		return this;
	}

	public Dan getHeavyLane() {
		return heavyLane;
	}

	public void setLightLane(Pipe lightLane) {
		this.lightLane = lightLane;
	}

	public SkewSplitter withLightLane(Pipe lightLane) {
		setLightLane(lightLane);
		return this;
	}

	public Pipe getLightLane() {
		return lightLane;
	}

	public void setSorter(Filter sorter) {
		this.sorter = sorter;
	}

	public SkewSplitter withSorter(Filter sorter) {
		setSorter(sorter);
		return this;
	}

	public Filter getSorter() {
		return sorter;
	}

	@Override
	public void setUpstream(Sup upstream) {
		heavyLane.setUpstream(upstream);
	}

	@Override
	public void setDownstream(Req downstream) {
		this.downstream = downstream;
		heavyLane.setDownstream(downstream);
	}

	@Override
	public void requestNext() {
		heavyLane.requestNext();
	}

	@Override
	public void receiveNext(Data next) {
		boolean passes = sorter.test(next);
		if (passes) {
			Object nextProcessed = lightLane.apply(next);
			downstream.deliver((Data) nextProcessed);
		} else {
			heavyLane.deliver(next);
		}
	}

}
