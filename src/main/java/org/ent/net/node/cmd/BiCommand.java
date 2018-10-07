package org.ent.net.node.cmd;

import org.ent.net.NetController;
import org.ent.net.node.BNode;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.operation.BiOperation;

public class BiCommand<H1, H2> implements Command {

	private final Accessor<H1> accessor1;

	private final Accessor<H2> accessor2;

	private final BiOperation<H1, H2> operation;

	private String shortName;

	public BiCommand(Accessor<H1> accessor1, Accessor<H2> accessor2, BiOperation<H1, H2> operation) {
		this.accessor1 = accessor1;
		this.accessor2 = accessor2;
		this.operation = operation;
	}

	@Override
	public ExecutionResult execute(NetController controller, Node parameters) {
        if (!(parameters instanceof BNode)) return ExecutionResult.ERROR;
        BNode top = (BNode) parameters;
        return executeImpl(controller, top.getLeftChild(controller), top.getRightChild(controller));
	}

	private ExecutionResult executeImpl(NetController controller, Node arg1, Node arg2) {
		return accessor1.get(controller, arg1).flatMap(handle1 -> {
			return accessor2.get(controller, arg2).map(handle2 -> {
				return operation.apply(controller, handle1, handle2);
			});
		}).orElse(ExecutionResult.ERROR);
	}

	@Override
	public String getShortName() {
		if (shortName == null) {
			shortName = accessor1.getShortName() + operation.getShortName() + accessor2.getShortName();
		}
		return shortName;
	}

}
