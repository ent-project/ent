package org.ent.net.node.cmd;

import org.ent.net.Arrow;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.operation.BiOperation;

public class BiCommand implements Command {

	private final Accessor accessor1;

	private final Accessor accessor2;

	private final BiOperation operation;

	private final int value;

	private String shortName;

	private String shortNameAscii;

	public BiCommand(Accessor accessor1, Accessor accessor2, BiOperation operation) {
		this.accessor1 = accessor1;
		this.accessor2 = accessor2;
		this.operation = operation;
		this.value = (accessor1.getCode() << 8) | (accessor2.getCode() << 12) | operation.getCode();
	}

	public BiOperation getOperation() {
		return operation;
	}

	@Override
	public ExecutionResult execute(Node parameters) {
		return executeImpl(parameters.getLeftArrow(), parameters.getRightArrow());
	}

	private ExecutionResult executeImpl(Arrow arg1, Arrow arg2) {
		return operation.apply(accessor1.get(arg1, Purview.COMMAND), accessor2.get(arg2, Purview.COMMAND));
	}

	@Override
	public int getValue() {
		return value;
	}

	@Override
	public String getShortName() {
		if (shortName == null) {
			shortName = accessor1.getShortName() + operation.getShortName() + accessor2.getShortName();
		}
		return shortName;
	}

	@Override
	public String getShortNameAscii() {
		if (shortNameAscii == null) {
			shortNameAscii = accessor1.getShortNameAscii() + operation.getShortNameAscii()
					+ accessor2.getShortNameAscii();
		}
		return shortNameAscii;
	}
}
