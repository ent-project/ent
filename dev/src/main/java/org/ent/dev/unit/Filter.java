package org.ent.dev.unit;

import org.ent.dev.unit.FilterWrapper.FilterListener;

public interface Filter {

	boolean test(Data data);

	default Filter with(FilterListener listener) {
		return new FilterWrapper(this, listener);
	}

	default Filter combineProc(Proc proc) {
		return new FilterWithProc(this, proc);
	}

}
