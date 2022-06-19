package org.ent.dev.unit.local;

import org.ent.dev.unit.data.Data;
import org.ent.dev.unit.data.DataProxy;

public abstract class TypedProc<T extends DataProxy> implements Proc {

	private final T accessor;

	protected TypedProc(Class<T> accessorType) {
		this.accessor = createInstance(accessorType);
	}

	public static <T> T createInstance(Class<T> type) {
		try {
			return type.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected abstract void doAccept(T data);

	@Override
	public void accept(Data data) {
		accessor.setDelegate(data);
		doAccept(accessor);
	}

}
