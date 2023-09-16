package org.ent.net.node.cmd.operation.math;

import org.ent.permission.Permissions;
import org.ent.permission.WriteFacet;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.net.node.cmd.operation.TriNodeOperation;

public class ModuloOperation extends TriNodeOperation {

    @Override
    public int getCode() {
        return Operations.CODE_MODULO_OPERATION;
    }

    @Override
    public ExecutionResult doApply(Node node1, Node node2, Node node3, Permissions permissions) {
        if (permissions.noWrite(node1, WriteFacet.VALUE)) return ExecutionResult.ERROR;

        int a = node2.getValue(permissions);
        int b = node3.getValue(permissions);
        if (b == 0) {
            return ExecutionResult.ERROR;
        }
        node1.setValue(compute(a, b), permissions);
        return ExecutionResult.NORMAL;
    }

    public int compute(int a, int b) {
        return a % b;
    }

    @Override
    public String getFirstSeparator() {
        return "=";
    }

    @Override
    public String getSecondSeparator() {
        return "%";
    }
}