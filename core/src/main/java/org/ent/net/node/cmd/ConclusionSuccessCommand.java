package org.ent.net.node.cmd;

import org.ent.net.node.Node;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.permission.Permissions;

public class ConclusionSuccessCommand extends NopCommand {

    @Override
    public ExecutionResult execute(Node base, Permissions permissions) {
        return ExecutionResult.CONCLUDED;
    }

    @Override
    public int getValueBase() {
        return Command.COMMAND_PATTERN | Command.IS_FINAL_FLAG | Operations.COMMAND_CODE_FINAL_SUCCESS;
    }

    @Override
    public String getShortName() {
        return "SUCCESS";
    }
}
