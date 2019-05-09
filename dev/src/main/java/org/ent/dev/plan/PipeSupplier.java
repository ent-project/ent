package org.ent.dev.plan;

public class PipeSupplier<S extends NetInfo, T extends NetInfo> implements Supplier<T> {

	private final Supplier<S> upstream;

	private final Pipe<S, T> delegate;

	public PipeSupplier(Supplier<S> upstream, Pipe<S, T> delegate) {
		this.upstream = upstream;
		this.delegate = delegate;
	}

	@Override
	public T next() {
		S nextFromUpstream = upstream.next();
		return delegate.process(nextFromUpstream);
	}

}
