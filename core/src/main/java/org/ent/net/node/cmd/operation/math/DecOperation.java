package org.ent.net.node.cmd.operation.math;

import org.ent.net.node.cmd.operation.MonoValueOperation;
import org.ent.net.node.cmd.operation.Operations;

public class DecOperation extends MonoValueOperation {

	@Override
	public int getCode() {
		return Operations.CODE_DEC_OPERATION;
	}

	@Override
	public int compute(int a) {
		return a - 1;
	}

	@Override
	public String getShortName() {
		return "dec";
	}
}
