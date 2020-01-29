package org.ent.dev.unit;

import java.util.ArrayDeque;
import java.util.Deque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Asynchronous call mechanism to avoid overflow of JVM call stack.
 */
public class DeliveryStash {

	private static final Logger log = LoggerFactory.getLogger(DeliveryStash.class);

	public static final DeliveryStash instance = new DeliveryStash();

	private Deque<Packet> stack = new ArrayDeque<>();

	private static class Packet {

		private final Data data;

		private final Req req;

		public Packet(Data data, Req req) {
			this.data = data;
			this.req = req;
		}

		public Data getData() {
			return data;
		}

		public Req getReq() {
			return req;
		}
	}

	public void submit(Data data, Req req) {
		if (!stack.isEmpty()) throw new AssertionError("stack must be empty when submitting an element");
		stack.push(new Packet(data, req));
	}

	public boolean hasWork() {
		return !stack.isEmpty();
	}

	public void work() {
		Packet packet = stack.pop();
		Data data = packet.getData();
		Req req = packet.getReq();
		if (log.isTraceEnabled()) {
			log.trace("work {} -> {}", data, req);
		}
		req.receiveNext(data);
	}

}
