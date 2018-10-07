package org.ent.net.node.cmd.operation;

import org.ent.net.NetController;
import org.ent.net.node.cmd.ExecutionResult;

public interface BiOperation<H1, H2> {

	ExecutionResult apply(NetController controller, H1 handle1, H2 handle2);

	String getShortName();
}
