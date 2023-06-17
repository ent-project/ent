package org.ent.net.node.cmd;

import org.ent.Ent;
import org.ent.net.Arrow;
import org.ent.net.AccessToken;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.accessor.DirectAccessor;
import org.ent.net.node.cmd.operation.MonoOperation;

public class MonoCommand extends VetoedCommand {

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
    protected ExecutionResult doExecute(Node base, Ent ent, AccessToken accessToken) {
        Arrow handle = accessor.get(base, ent, Purview.COMMAND);
        return operation.apply(handle, ent, accessToken);
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

    private String buildShortName() {
        String accessorName = this.accessor.getShortName();
        if (this.accessor instanceof DirectAccessor) {
            accessorName = "";
        }
        return this.operation.getShortName() + accessorName;
    }

    @Override
    public boolean isEval() {
        return operation.isEval();
    }
}
