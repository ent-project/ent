package org.ent.net.node.cmd.operation.math;

import org.ent.net.node.cmd.operation.BiValueOperation;
import org.ent.net.node.cmd.operation.Operations;

public class DecOperation extends BiValueOperation {

	@Override
	public int getCode() {
		return Operations.CODE_DEC_OPERATION;
	}

	@Override
	public int compute(int a) {
		return a - 1;
	}

	@Override
	public String getShortNameAscii() {
		return "=-1+";
	}
}
