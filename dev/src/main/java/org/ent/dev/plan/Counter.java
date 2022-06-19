package org.ent.dev.plan;

import org.ent.dev.unit.local.TypedProc;

public class Counter extends TypedProc<CounterData> {

	private long currentNumber;

	public Counter() {
		super(CounterData.class);
	}

	@Override
	public void doAccept(CounterData data) {
		currentNumber++;

		data.setSerialNumber(currentNumber);
	}

}
