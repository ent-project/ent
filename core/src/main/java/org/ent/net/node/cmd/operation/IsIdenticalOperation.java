package org.ent.net.node.cmd.operation;

import org.ent.net.Arrow;
import org.ent.net.Manner;
import org.ent.net.node.cmd.ExecutionResult;

public class IsIdenticalOperation implements BiOperation<Arrow, Arrow> {

	@Override
	public ExecutionResult apply(Arrow arrow1, Arrow arrow2) {
		boolean condition = evaluateCondition(arrow1, arrow2);
		if (condition) {
			arrow1.setTarget(arrow1.getOrigin(), Manner.COMMAND);
		} else {
			arrow1.setTarget(arrow2.getOrigin(), Manner.COMMAND);
		}
		return ExecutionResult.NORMAL;
	}

	private boolean evaluateCondition(Arrow arrow1, Arrow arrow2) {
		return arrow1.getTarget(Manner.COMMAND) == arrow2.getTarget(Manner.COMMAND);
	}

	@Override
	public String getShortName() {
		return "≡";
	}

	@Override
	public String getShortNameAscii() {
		return "==";
	}

}
