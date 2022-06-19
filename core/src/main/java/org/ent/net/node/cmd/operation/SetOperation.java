package org.ent.net.node.cmd.operation;

import org.ent.net.Arrow;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public class SetOperation implements BiOperation<Arrow, Node> {

	@Override
	public ExecutionResult apply(Arrow setter, Node target) {
		setter.setTarget(target, Purview.COMMAND);
		return ExecutionResult.NORMAL;
	}

	@Override
	public String getShortName() {
		return "◄";
	}

	@Override
	public String getShortNameAscii() {
		return ":";
	}

}
