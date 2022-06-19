package org.ent.dev.unit.local;

import org.ent.dev.unit.combine.FilterWithProc;
import org.ent.dev.unit.data.Data;

public interface Filter {

	boolean test(Data data);

	default Filter with(FilterListener listener) {
		return new FilterWrapper(this, listener);
	}

	default Filter combineProc(Proc proc) {
		return new FilterWithProc(this, proc);
	}

}
