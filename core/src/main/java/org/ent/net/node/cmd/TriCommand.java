package org.ent.net.node.cmd;

import org.ent.Ent;
import org.ent.net.Arrow;
import org.ent.net.AccessToken;
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
    private final int valueBase;

    private final String shortName;

    public TriCommand(TriOperation operation, Accessor accessor1, Accessor accessor2, Accessor accessor3) {
        this.accessor1 = accessor1;
        this.accessor2 = accessor2;
        this.accessor3 = accessor3;
        this.operation = operation;
        this.valueBase = Command.COMMAND_PATTERN | operation.getCode();
        this.value = valueBase |
                (accessor1.getCode() << 12) |
                (accessor2.getCode() << 16) |
                (accessor3.getCode() << 20);
        this.shortName = accessor1.getShortName() + operation.getFirstSeparator() +
                accessor2.getShortName() + operation.getSecondSeparator() + accessor3.getShortName();
    }

    public TriOperation getOperation() {
        return operation;
    }

    public Accessor getAccessor1() {
        return accessor1;
    }

    public Accessor getAccessor2() {
        return accessor2;
    }

    public Accessor getAccessor3() {
        return accessor3;
    }

    @Override
    public ExecutionResult doExecute(Node base, Ent ent, AccessToken accessToken) {
        Arrow handle1 = accessor1.get(base, ent, Purview.COMMAND);
        Arrow handle2 = accessor2.get(base, ent, Purview.COMMAND);
        Arrow handle3 = accessor3.get(base, ent, Purview.COMMAND);
        return operation.apply(handle1, handle2, handle3, ent, accessToken);
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public int getValueBase() {
        return valueBase;
    }

    public String getShortName() {
        return shortName;
    }

    @Override
    public int getNumberOfParameters() {
        return 3;
    }
}
