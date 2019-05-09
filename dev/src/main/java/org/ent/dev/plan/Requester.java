package org.ent.dev.plan;

public interface Requester<T> {

	void setUpstream(Supplier<T> upstream);

	Supplier<T> getUpstream();
}
