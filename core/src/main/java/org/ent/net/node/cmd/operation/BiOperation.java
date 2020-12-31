package org.ent.net.node.cmd.operation;

import org.ent.net.node.cmd.ExecutionResult;

public interface BiOperation<H1, H2> {

	ExecutionResult apply(H1 handle1, H2 handle2);

	default int getEvalLevel() {
		return 0;
	}

	String getShortName();

	String getShortNameAscii();
}
