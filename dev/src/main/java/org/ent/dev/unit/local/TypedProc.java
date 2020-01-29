package org.ent.dev.unit.local;

import org.ent.dev.unit.data.Data;
import org.ent.dev.unit.data.DataProxy;

public abstract class TypedProc<T extends DataProxy> implements Proc {

	private final T accessor;

	public TypedProc(T accessor) {
		this.accessor = accessor;
	}

	protected abstract void doAccept(T Data);

	@Override
	public void accept(Data data) {
		accessor.setDelegate(data);
		doAccept(accessor);
	}

}
