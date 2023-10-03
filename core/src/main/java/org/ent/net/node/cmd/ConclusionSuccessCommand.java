package org.ent.net.node.cmd;

import org.ent.net.node.cmd.operation.Operations;

public class ConclusionSuccessCommand extends NopCommand {

    @Override
    public int getValueBase() {
        return Command.COMMAND_PATTERN | Operations.COMMAND_CODE_FINAL_SUCCESS;
    }

    @Override
    public String getShortName() {
        return "SUCCESS";
    }

    @Override
    public boolean isConcluding() {
        return true;
    }
}
