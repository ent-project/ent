package org.ent.net.node.cmd;

import org.ent.net.Arrow;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.operation.BiOperation;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.net.node.cmd.veto.Veto;
import org.ent.net.node.cmd.veto.Vetos;
import org.ent.permission.Permissions;

public class BranchingBiCommand implements Command {

    private final Accessor accessor1;
    private final Accessor accessor2a;
    private final Accessor accessor2b;

    private final BiOperation operation;

    private final int value;
    private final int valueBase;

    private final String shortName;

    public BranchingBiCommand(BiOperation operation, Accessor accessor1, Accessor accessor2a, Accessor accessor2b) {
        this.operation = operation;
        this.accessor1 = accessor1;
        this.accessor2a = accessor2a;
        this.accessor2b = accessor2b;
        this.valueBase = Command.COMMAND_PATTERN | Operations.CODE_BRANCHING_SET_OPERATION;
        this.value = valueBase |
                (accessor1.getCode() << 12) |
                (accessor2a.getCode() << 16) |
                (accessor2b.getCode() << 20);
        this.shortName = accessor1.getShortName() + operation.getShortName() +
                accessor2a.getShortName() + "o" + accessor2b.getShortName();
    }

    @Override
    public ExecutionResult execute(Node base, Permissions permissions) {
        Node conditionNode = base.getLeftChild(permissions);
        int conditionalue = conditionNode.getValue(permissions);
        Veto condition = Vetos.getByValue(conditionalue);
        if (condition == null) {
            return ExecutionResult.NORMAL;
        } else {
            boolean pass = condition.evaluate(conditionNode, permissions);
            return doExecute(base, pass, permissions);
        }
    }

    protected ExecutionResult doExecute(Node base, boolean pass, Permissions permissions) {
        Arrow handle1 = accessor1.get(base, permissions);
        Arrow handle2 = pass ? accessor2a.get(base, permissions) : accessor2b.get(base, permissions);
        return operation.apply(handle1, handle2, permissions);
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
        return 3;
    }
}
