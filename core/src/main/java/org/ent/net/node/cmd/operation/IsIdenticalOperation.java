package org.ent.net.node.cmd.operation;

import org.ent.net.Arrow;
import org.ent.net.Purview;
import org.ent.net.node.cmd.ExecutionResult;

public class IsIdenticalOperation implements BiOperation {

	@Override
	public int getCode() {
		return Operations.CODE_IS_IDENTICAL_OPERATION;
	}

	@Override
	public ExecutionResult apply(Arrow arrow1, Arrow arrow2) {
		boolean condition = evaluateCondition(arrow1, arrow2);
		if (condition) {
			arrow1.setTarget(arrow1.getOrigin(), Purview.COMMAND);
		} else {
			arrow1.setTarget(arrow2.getOrigin(), Purview.COMMAND);
		}
		return ExecutionResult.NORMAL;
	}

	private boolean evaluateCondition(Arrow arrow1, Arrow arrow2) {
		return arrow1.getTarget(Purview.COMMAND) == arrow2.getTarget(Purview.COMMAND);
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
