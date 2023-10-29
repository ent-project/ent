package org.ent.net.node.cmd;

import org.ent.net.Arrow;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.operation.BiOperation;
import org.ent.permission.Permissions;

public class BiCommand implements Command {

	private final Accessor accessor1;

	private final Accessor accessor2;

	private final BiOperation operation;

	private final int value;

	private final int valueBase;

	private final String shortName;

	public BiCommand(BiOperation operation, Accessor accessor1, Accessor accessor2) {
		this.operation = operation;
		this.accessor1 = accessor1;
		this.accessor2 = accessor2;
		this.valueBase = Command.COMMAND_PATTERN | operation.getCode() ;
		this.value = valueBase | (accessor1.getCode() << 12) | (accessor2.getCode() << 16);
		this.shortName = buildShortName();
	}

	@Override
	public ExecutionResult execute(Node base, Permissions permissions) {
		Arrow handle1 = accessor1.get(base, permissions);
		Arrow handle2 = accessor2.get(base, permissions);
		return operation.apply(handle1, handle2, permissions);
	}

	public BiOperation getOperation() {
		return operation;
	}

	@Override
	public int getValueBase() {
		return valueBase;
	}

	@Override
	public int getValue() {
		return value;
	}

	@Override
	public boolean isEval() {
		return operation.isEval();
	}

	public String getShortName() {
		return shortName;
	}

	@Override
	public int getNumberOfParameters() {
		return 2;
	}

	public Accessor getAccessor1() {
		return accessor1;
	}

	public Accessor getAccessor2() {
		return accessor2;
	}

	private String buildShortName() {
		String accessor1Name = this.accessor1.getShortName();
		String accessor2Name = this.accessor2.getShortName();
		return accessor1Name + this.operation.getShortName() + accessor2Name;
	}

	@Override
	public String toString() {
		return "<" + getShortName() + ">";
	}
}
