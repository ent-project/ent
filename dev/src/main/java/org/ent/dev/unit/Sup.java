package org.ent.dev.unit;

/**
 * "Sup": short for supplier
 */
public interface Sup {

	void setDownstream(Req downstream);

	void requestNext();

	default Dan connectDan(Dan downstream) {
		downstream.setUpstream(this);
		this.setDownstream(downstream);
		return downstream;
	}

	default <T extends Req> T connectReq(T downstream) {
		downstream.setUpstream(this);
		this.setDownstream(downstream);
		return downstream;
	}

	default Sup combineDan(Dan downstream) {
		return new SupWithDan(this, downstream);
	}

	default Sup combinePipe(Pipe pipe) {
		return combineDan(new PipeDan(pipe));
	}

	default Sup combineProc(Proc proc) {
		return combinePipe(proc);
	}

	default Sup combineFilter(Filter filter) {
		return combineDan(new FilterDan(filter));
	}

	default Sup combine(Dan downstream) {
		return combineDan(downstream);
	}

	default Sup combine(Pipe pipe) {
		return combinePipe(pipe);
	}

	default Sup combine(Filter filter) {
		return combineFilter(filter);
	}

}
