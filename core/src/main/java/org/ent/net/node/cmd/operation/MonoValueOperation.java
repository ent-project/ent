package org.ent.net.node.cmd.operation;

import org.ent.permission.Permissions;
import org.ent.permission.WriteFacet;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public abstract class MonoValueOperation extends MonoNodeOperation {

    @Override
    public ExecutionResult doApply(Node node, Permissions permissions) {
        if (permissions.noWrite(node, WriteFacet.VALUE)) return ExecutionResult.ERROR;
        int newValue = compute(node.getValue(permissions));
        node.setValue(newValue, permissions);
        return ExecutionResult.NORMAL;
    }

    public abstract int compute(int a);
}
