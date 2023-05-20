package org.ent.net.node.cmd;

import org.ent.Ent;
import org.ent.net.Arrow;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.operation.TriOperation;

public class TriCommand extends VetoedCommand {

    private final Accessor accessor1;
    private final Accessor accessor2;
    private final Accessor accessor3;

    private final TriOperation operation;

    private final int value;

    private final String shortName;

    public TriCommand(Accessor accessor1, Accessor accessor2, Accessor accessor3, TriOperation operation) {
        this.accessor1 = accessor1;
        this.accessor2 = accessor2;
        this.accessor3 = accessor3;
        this.operation = operation;
        this.value = Command.COMMAND_PATTERN |
                operation.getCode() |
                (accessor1.getCode() << 12) |
                (accessor2.getCode() << 16) |
                (accessor3.getCode() << 20);
        this.shortName = accessor1.getShortName() + operation.getFirstSeparator() +
                accessor2.getShortName() + operation.getSecondSeparator() + accessor3.getShortName();
    }

    public TriOperation getOperation() {
        return operation;
    }

    @Override
    public ExecutionResult doExecute(Node base, Ent ent) {
        Arrow handle1 = accessor1.get(base, ent, Purview.COMMAND);
        Arrow handle2 = accessor2.get(base, ent, Purview.COMMAND);
        Arrow handle3 = accessor3.get(base, ent, Purview.COMMAND);
        return operation.apply(handle1, handle2, handle3);
    }

    @Override
    public int getValue() {
        return value;
    }

    public String getShortName() {
        return shortName;
    }
}
