package org.ent.dev.unit;

public interface Req {

	void setUpstream(Sup upstream);

	void receiveNext(Data next);

	default void deliver(Data next) {
		DeliveryStash.instance.submit(next, this);
	}
}
