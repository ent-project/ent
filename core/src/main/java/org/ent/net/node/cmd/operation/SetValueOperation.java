package org.ent.net.node.cmd.operation;

public class SetValueOperation extends BiValueOperation {

	@Override
	public int getCode() {
		return Operations.CODE_SET_VALUE_OPERATION;
	}

	@Override
	public int compute(int a) {
		return a;
	}

	@Override
	public String getShortNameAscii() {
		return "=+";
	}
}
