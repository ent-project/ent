package org.ent.net.node.cmd.operation;

import org.ent.permission.Permissions;
import org.ent.net.Arrow;
import org.ent.net.node.cmd.ExecutionResult;

public interface TriOperation {

	int getCode();

	ExecutionResult apply(Arrow handle1, Arrow handle2, Arrow handle3, Permissions permissions);

	String getFirstSeparator();

	String getSecondSeparator();
}
