package org.ent.net.node.cmd.operation.math;

import org.ent.net.node.cmd.operation.BiValueOperation;
import org.ent.net.node.cmd.operation.Operations;

public class IncOperation extends BiValueOperation {

	@Override
	public int getCode() {
		return Operations.CODE_INC_OPERATION;
	}

	@Override
	public int compute(int a) {
		return a + 1;
	}

	@Override
	public String getShortName() {
		return "inc";
	}
}
