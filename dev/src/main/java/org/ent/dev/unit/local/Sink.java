package org.ent.dev.unit.local;

import org.ent.dev.unit.Req;
import org.ent.dev.unit.combine.SinkReq;
import org.ent.dev.unit.data.Data;

public interface Sink {

	void receive(Data data);

	default Req toReq() {
		return new SinkReq(this);
	}

}
