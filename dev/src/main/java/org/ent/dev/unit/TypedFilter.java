package org.ent.dev.unit;

public abstract class TypedFilter<T extends DataProxy> implements Filter {

	private final T accessor;

	public TypedFilter(T accessor) {
		this.accessor = accessor;
	}

	protected abstract boolean doTest(T Data);

	@Override
	public boolean test(Data data) {
		accessor.setDelegate(data);
		return doTest(accessor);
	}

}
