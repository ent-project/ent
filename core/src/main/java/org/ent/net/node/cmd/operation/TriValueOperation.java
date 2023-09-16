package org.ent.net.node.cmd.operation;

import org.ent.permission.Permissions;
import org.ent.permission.WriteFacet;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public abstract class TriValueOperation extends TriNodeOperation {
    @Override
    public ExecutionResult doApply(Node node1, Node node2, Node node3, Permissions permissions) {
        if (permissions.noWrite(node1, WriteFacet.VALUE)) return ExecutionResult.ERROR;
        node1.setValue(compute(node2.getValue(permissions), node3.getValue(permissions)), permissions);
        return ExecutionResult.NORMAL;
    }

    public abstract int compute(int a, int b);

    @Override
    public String getFirstSeparator() {
        return "=";
    }
}
