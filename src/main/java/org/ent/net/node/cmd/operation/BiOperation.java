package org.ent.net.node.cmd.operation;

import org.ent.net.NetController;

public interface BiOperation<H1, H2> {

	void apply(NetController controller, H1 handle1, H2 handle2);

	String getShortName();
}
