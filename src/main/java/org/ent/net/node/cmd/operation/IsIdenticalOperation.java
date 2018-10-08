package org.ent.net.node.cmd.operation;

import org.ent.net.Arrow;
import org.ent.net.NetController;
import org.ent.net.node.cmd.ExecutionResult;

public class IsIdenticalOperation implements BiOperation<Arrow, Arrow> {

	@Override
	public ExecutionResult apply(NetController controller, Arrow arrow1, Arrow arrow2) {
		boolean condition = evaluateCondition(controller, arrow1, arrow2);
		if (condition) {
			arrow1.setTarget(controller, arrow1.getOrigin());
		} else {
			arrow1.setTarget(controller, arrow2.getOrigin());
		}
		return ExecutionResult.NORMAL;
	}

	private boolean evaluateCondition(NetController controller, Arrow arrow1, Arrow arrow2) {
		return arrow1.getTarget(controller) == arrow2.getTarget(controller);
	}

	@Override
	public String getShortName() {
		return "≡";
	}

}
