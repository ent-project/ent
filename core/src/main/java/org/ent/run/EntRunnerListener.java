package org.ent.run;

import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public interface EntRunnerListener {

    void fireCommandExecuted(Node commandNode, ExecutionResult executeResult);
}
