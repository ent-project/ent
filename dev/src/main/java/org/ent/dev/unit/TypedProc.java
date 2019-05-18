package org.ent.dev.unit;

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
