package org.ent.net.node.cmd.operation;

import org.ent.net.Arrow;
import org.ent.net.node.cmd.ExecutionResult;

public interface BiOperation {

	int getCode();

	ExecutionResult apply(Arrow handle1, Arrow handle2);

	String getShortName();

	String getShortNameAscii();
}
