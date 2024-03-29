package org.ent.net.node.cmd;

import org.ent.net.Arrow;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.operation.MonoOperation;
import org.ent.permission.Permissions;

public class MonoCommand implements Command {

    private final Accessor accessor;

    private final MonoOperation operation;

    private final int value;

    private final int valueBase;

    private final String shortName;

    public MonoCommand(MonoOperation operation, Accessor accessor) {
        this.operation = operation;
        this.accessor = accessor;
        this.valueBase = Command.COMMAND_PATTERN | operation.getCode() ;
        this.value = valueBase | (accessor.getCode() << 12);
        this.shortName = buildShortName();
    }

    @Override
    public ExecutionResult execute(Node base, Permissions permissions) {
        Arrow handle = accessor.get(base, permissions);
        return operation.apply(handle, permissions);
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
    public int getValueBase() {
        return valueBase;
    }

    @Override
    public int getNumberOfParameters() {
        return 1;
    }

    public Accessor getAccessor() {
        return accessor;
    }

    private String buildShortName() {
        String accessorName = this.accessor.getShortName();
        return this.operation.getShortName() + accessorName;
    }

    @Override
    public boolean isEval() {
        return operation.isEval();
    }

    @Override
    public String toString() {
        return "<" + getShortName() + ">";
    }
}
