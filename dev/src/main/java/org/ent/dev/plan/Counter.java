package org.ent.dev.plan;

import org.ent.dev.unit.TypedProc;

public class Counter extends TypedProc<CounterData> {

	private long currentNumber;

	public Counter() {
		super(new CounterData());
	}

	@Override
	public void doAccept(CounterData data) {
		currentNumber++;

		data.setSerialNumber(currentNumber);
	}

}
