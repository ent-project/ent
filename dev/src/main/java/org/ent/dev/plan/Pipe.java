package org.ent.dev.plan;

public interface Pipe<S, T> {

	T process(S input);

	default Processor<S, T> toProcessor() {
		return new PipeProcessor<S, T>(this);
	}

}
