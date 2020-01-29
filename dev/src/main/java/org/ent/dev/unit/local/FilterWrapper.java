package org.ent.dev.unit.local;

import org.ent.dev.unit.data.Data;

public class FilterWrapper implements Filter {

	private Filter delegate;

	private FilterListener listener;

	public interface FilterListener {
		void success(Data data);
		void failure(Data data);
	}

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
