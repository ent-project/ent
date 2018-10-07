package org.ent.net.node.cmd;

import org.ent.net.NetController;
import org.ent.net.node.Node;

public interface Command {

    ExecutionResult execute(NetController controller, Node parameters);

	String getShortName();

    default String getShortNameAscii() {
        return getShortName();
    }

}
