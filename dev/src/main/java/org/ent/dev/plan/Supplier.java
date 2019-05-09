package org.ent.dev.plan;

public interface Supplier<T> {

	T next();

	default <U> Processor<T, U> connect(Processor<T, U> downstream) {
		downstream.setUpstream(this);
		return downstream;
	}

	default <U> Processor<T, U> connect(Pipe<T, U> downstreamPipe) {
		Processor<T, U> downstreamProcessor = downstreamPipe.toProcessor();
		downstreamProcessor.setUpstream(this);
		return downstreamProcessor;
	}

}
