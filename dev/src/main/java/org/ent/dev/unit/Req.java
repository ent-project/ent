package org.ent.dev.unit;

import org.ent.dev.unit.data.Data;

public interface Req {

	void setUpstream(Sup upstream);

	void receiveNext(Data next);

	default void deliver(Data next) {
		DeliveryStash.instance.submit(next, this);
	}
}
