package org.ent.net.node.cmd;

import org.ent.net.Arrow;
import org.ent.net.Purview;
import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.operation.TriOperation;

public class TriCommand implements Command {

    private final Accessor accessor1;
    private final Accessor accessor2;
    private final Accessor accessor3;

    private final TriOperation operation;

    private final int value;

    private final String shortName;

    private final String shortNameAscii;

    public TriCommand(Accessor accessor1, Accessor accessor2, Accessor accessor3, TriOperation operation) {
        this.accessor1 = accessor1;
        this.accessor2 = accessor2;
        this.accessor3 = accessor3;
        this.operation = operation;
        this.value = operation.getCode() | (accessor1.getCode() << 8) | (accessor2.getCode() << 12) | (accessor3.getCode() << 16);
        this.shortNameAscii = accessor1.getShortNameAscii() + operation.getFirstSeparator() +
                accessor2.getShortNameAscii() + operation.getSecondSeparator() + accessor3.getShortNameAscii();
        this.shortName = accessor1.getShortName() + operation.getFirstSeparator() + accessor2.getShortName() +
                operation.getSecondSeparator() + accessor3.getShortName();
    }

    public TriOperation getOperation() {
        return operation;
    }

    @Override
    public ExecutionResult execute(Arrow parameters) {
        return operation.apply(accessor1.get(parameters, Purview.COMMAND), accessor2.get(parameters, Purview.COMMAND), accessor3.get(parameters, Purview.COMMAND));
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    @Override
    public String getShortNameAscii() {
        return shortNameAscii;
    }
}
