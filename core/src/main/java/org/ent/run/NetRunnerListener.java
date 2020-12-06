package org.ent.run;

import org.ent.net.node.CNode;
import org.ent.net.node.cmd.ExecutionResult;

public interface NetRunnerListener {

    void fireCommandExecuted(CNode commandNode, ExecutionResult executeResult);
}
