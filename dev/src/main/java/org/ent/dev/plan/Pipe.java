package org.ent.dev.plan;

public interface Pipe<S, T> {

	T process(S input);

}
