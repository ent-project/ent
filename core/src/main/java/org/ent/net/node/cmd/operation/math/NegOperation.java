package org.ent.net.node.cmd.operation.math;

import org.ent.net.node.cmd.operation.BiValueOperation;
import org.ent.net.node.cmd.operation.Operations;

public class NegOperation extends BiValueOperation {

	@Override
	public int getCode() {
		return Operations.CODE_NEG_OPERATION;
	}

	@Override
	public int compute(int a) {
		return -a;
	}

	@Override
	public String getShortNameAscii() {
		return "=-";
	}
}
