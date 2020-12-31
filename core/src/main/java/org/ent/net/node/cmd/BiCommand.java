package org.ent.net.node.cmd;

import org.ent.net.Manner;
import org.ent.net.node.BNode;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.accessor.NodeAccessor;
import org.ent.net.node.cmd.operation.BiOperation;

public class BiCommand<H1, H2> implements Command {

	private final Accessor<H1> accessor1;

	private final Accessor<H2> accessor2;

	private final BiOperation<H1, H2> operation;

	private String shortName;

	private String shortNameAscii;

	public BiCommand(Accessor<H1> accessor1, Accessor<H2> accessor2, BiOperation<H1, H2> operation) {
		this.accessor1 = accessor1;
		this.accessor2 = accessor2;
		this.operation = operation;
	}

	public BiOperation<H1, H2> getOperation() {
		return operation;
	}

	@Override
	public ExecutionResult execute(Node parameters) {
        if (!(parameters instanceof BNode top)) return ExecutionResult.ERROR;
		return executeImpl(top.getLeftChild(Manner.COMMAND), top.getRightChild(Manner.COMMAND));
	}

	private ExecutionResult executeImpl(Node arg1, Node arg2) {
		return accessor1.get(arg1, Manner.COMMAND).flatMap(handle1 ->
			accessor2.get(arg2, Manner.COMMAND).map(handle2 ->
				operation.apply(handle1, handle2)
			)
		).orElse(ExecutionResult.ERROR);
	}

	@Override
	public int getEvalLevel() {
		return operation.getEvalLevel();
	}

	@Override
	public String getShortName() {
		if (shortName == null) {
			if (isTwoNodeAccessorShortcut()) {
				shortName = operation.getShortName();
			} else {
				shortName = accessor1.getShortName() + operation.getShortName() + accessor2.getShortName();
			}
		}
		return shortName;
	}

	@Override
	public String getShortNameAscii() {
		if (shortNameAscii == null) {
			if (isTwoNodeAccessorShortcut()) {
				shortNameAscii = operation.getShortNameAscii();
			} else {
				shortNameAscii = accessor1.getShortNameAscii() + operation.getShortNameAscii()
						+ accessor2.getShortNameAscii();
			}
		}
		return shortNameAscii;
	}

	private boolean isTwoNodeAccessorShortcut() {
		return accessor1 instanceof NodeAccessor && accessor2 instanceof NodeAccessor;
	}

}
