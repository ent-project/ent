package org.ent.run;

import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public interface NetRunnerListener {

    void fireCommandExecuted(Node commandNode, ExecutionResult executeResult);
}
