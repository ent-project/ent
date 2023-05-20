package org.ent.net.node.cmd;

import org.ent.Ent;
import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Purview;
import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.accessor.PrimaryAccessor;
import org.ent.net.node.cmd.operation.BiOperation;

public class BiCommand extends VetoedCommand {

	private final Accessor accessor1;

	private final Accessor accessor2;

	private final BiOperation operation;

	private final int value;

	private final String shortName;

	public BiCommand(Accessor accessor1, Accessor accessor2, BiOperation operation) {
		this.accessor1 = accessor1;
		this.accessor2 = accessor2;
		this.operation = operation;
		this.value = operation.getCode() | (accessor1.getCode() << 8) | (accessor2.getCode() << 12);
		this.shortName = buildShortName();
	}

	public BiOperation getOperation() {
		return operation;
	}

	@Override
	public ExecutionResult doExecute(Arrow parameters, Ent ent) {
		Arrow handle1 = accessor1.get(parameters, ent, Purview.COMMAND);
		Arrow handle2 = accessor2.get(parameters, ent, Purview.COMMAND);
		return operation.apply(handle1, handle2, ent);
	}

	@Override
	public int getValue() {
		return value;
	}

	public String getShortName() {
		return shortName;
	}

	private String buildShortName() {
		String accessor1Name = this.accessor1.getShortName();
		if (this.accessor1 instanceof PrimaryAccessor primaryLeft && primaryLeft.getDirection() == ArrowDirection.LEFT) {
			accessor1Name = "";
		}
		String accessor2Name = this.accessor2.getShortName();
		if (this.accessor2 instanceof PrimaryAccessor primaryRight && primaryRight.getDirection() == ArrowDirection.RIGHT) {
			accessor2Name = "";
		}
		return accessor1Name + this.operation.getShortName() + accessor2Name;
	}
}
