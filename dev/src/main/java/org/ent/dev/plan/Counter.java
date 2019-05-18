package org.ent.dev.plan;

import org.ent.dev.plan.DataProperties.PropSerialNumber;
import org.ent.dev.unit.Data;
import org.ent.dev.unit.Proc;

public class Counter implements Proc {

	private long currentNumber;

	@Override
	public void accept(Data input) {
		currentNumber++;

		((PropSerialNumber) input).setSerialNumber(currentNumber);
	}

}
