package org.ent.dev.unit;

public interface Sink {

	void receive(Data data);

	default Req toReq() {
		return new SinkReq(this);
	}

}
