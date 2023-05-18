package org.ent.net.node.cmd;

import org.ent.Ent;
import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Purview;
import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.accessor.PrimaryAccessor;
import org.ent.net.node.cmd.operation.BiOperation;

public class BiCommand implements Command {

	private final Accessor accessor1;

	private final Accessor accessor2;

	private final BiOperation operation;

	private final int value;

	private final String shortName;

	private final String shortNameAscii;

	public BiCommand(Accessor accessor1, Accessor accessor2, BiOperation operation) {
		this.accessor1 = accessor1;
		this.accessor2 = accessor2;
		this.operation = operation;
		this.value = operation.getCode() | (accessor1.getCode() << 8) | (accessor2.getCode() << 12);
		this.shortNameAscii = buildShortNameAscii();
		this.shortName = buildShortName();
	}

	public BiOperation getOperation() {
		return operation;
	}

	@Override
	public ExecutionResult execute(Arrow parameters, Ent ent) {
		return operation.apply(accessor1.get(parameters, ent, Purview.COMMAND), accessor2.get(parameters, ent, Purview.COMMAND));
	}

	@Override
	public int getValue() {
		return value;
	}

	@Override
	public String getShortName() {
		return shortName;
	}

	@Override
	public String getShortNameAscii() {
		return shortNameAscii;
	}

	private String buildShortNameAscii() {
		String accessor1Name;
		if (this.accessor1 instanceof PrimaryAccessor primaryLeft && primaryLeft.getDirection() == ArrowDirection.LEFT) {
			accessor1Name = "";
		} else {
			accessor1Name = this.accessor1.getShortNameAscii();
		}
		String accessor2Name;
		if (this.accessor2 instanceof PrimaryAccessor primaryRight && primaryRight.getDirection() == ArrowDirection.RIGHT) {
			accessor2Name = "";
		} else {
			accessor2Name = this.accessor2.getShortNameAscii();
		}
		return accessor1Name + this.operation.getShortNameAscii() + accessor2Name;
	}

	private String buildShortName() {
		String accessor1Name;
		if (this.accessor1 instanceof PrimaryAccessor primaryLeft && primaryLeft.getDirection() == ArrowDirection.LEFT) {
			accessor1Name = "";
		} else {
			accessor1Name = this.accessor1.getShortName();
		}
		String accessor2Name;
		if (this.accessor2 instanceof PrimaryAccessor primaryRight && primaryRight.getDirection() == ArrowDirection.RIGHT) {
			accessor2Name = "";
		} else {
			accessor2Name = this.accessor2.getShortName();
		}
		return accessor1Name + this.operation.getShortName() + accessor2Name;
	}
}
