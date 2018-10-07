package org.ent.net.node.cmd.operation;

import org.ent.net.NetController;
import org.ent.net.node.cmd.ExecuteResult;

public interface BiOperation<H1, H2> {

	ExecuteResult apply(NetController controller, H1 handle1, H2 handle2);

	String getShortName();
}
