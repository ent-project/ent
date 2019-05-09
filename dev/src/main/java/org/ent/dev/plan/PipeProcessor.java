package org.ent.dev.plan;

public class PipeProcessor<S, T> implements Processor<S, T> {

	private Supplier<S> upstream;

	private final Pipe<S, T> delegate;

	public PipeProcessor(Pipe<S, T> delegate) {
		this.delegate = delegate;
	}

	public PipeProcessor(Supplier<S> upstream, Pipe<S, T> delegate) {
		this.upstream = upstream;
		this.delegate = delegate;
	}

	@Override
	public void setUpstream(Supplier<S> upstream) {
		this.upstream = upstream;
	}

	public PipeProcessor<S,T> withUpstream(Supplier<S> upstream) {
		setUpstream(upstream);
		return this;
	}

	@Override
	public Supplier<S> getUpstream() {
		return upstream;
	}
	@Override
	public T next() {
		S nextFromUpstream = upstream.next();
		return delegate.process(nextFromUpstream);
	}

}
