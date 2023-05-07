package org.ent.net.node.cmd;

import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.accessor.Accessors;
import org.ent.net.node.cmd.operation.BiOperation;
import org.ent.net.node.cmd.operation.Operations;

public class Commands {

    public static final Command NOP = CommandFactory.getByValue(new NopCommand().getValue());
    public static final Command ANCESTOR_EXCHANGE = get(Operations.ANCESTOR_EXCHANGE);

    private Commands() {
    }

    public static Command get(Accessor left, BiOperation operation, Accessor right) {
        BiCommand command = new BiCommand(left, right, operation);
        return CommandFactory.getByValue(command.getValue());
    }

    public static Command get(BiOperation operation) {
        BiCommand command = new BiCommand(Accessors.DIRECT, Accessors.DIRECT, operation);
        return CommandFactory.getByValue(command.getValue());
    }



}
