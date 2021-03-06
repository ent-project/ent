package org.ent.dev.unit.combine;

import org.ent.dev.unit.data.Data;
import org.ent.dev.unit.local.Filter;
import org.ent.dev.unit.local.Proc;

public class FilterWithProc implements Filter {

	private final Filter filterDelegate;

	private final Proc procDelegate;

	public FilterWithProc(Filter filterDelegate, Proc procDelegate) {
		this.filterDelegate = filterDelegate;
		this.procDelegate = procDelegate;
	}

	@Override
	public boolean test(Data data) {
		boolean result = filterDelegate.test(data);
		if (result) {
			procDelegate.accept(data);
		}
		return result;
	}

}
