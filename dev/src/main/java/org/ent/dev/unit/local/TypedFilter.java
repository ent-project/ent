package org.ent.dev.unit.local;

import org.ent.dev.unit.data.Data;
import org.ent.dev.unit.data.DataProxy;

public abstract class TypedFilter<T extends DataProxy> implements Filter {

	private final T accessor;

	public TypedFilter(T accessor) {
		this.accessor = accessor;
	}

	protected abstract boolean doTest(T data);

	@Override
	public boolean test(Data data) {
		accessor.setDelegate(data);
		return doTest(accessor);
	}

}
