package org.ent.dev.unit.local;

import org.ent.dev.unit.data.Data;

public class FilterWrapper implements Filter {

	private final Filter delegate;

	private final FilterListener listener;

	public FilterWrapper(Filter delegate, FilterListener listener) {
		this.delegate = delegate;
		this.listener = listener;
	}

	@Override
	public boolean test(Data data) {
		boolean result = delegate.test(data);
		if (result) {
			listener.success(data);
		} else {
			listener.failure(data);
		}
		return result;
	}

}
