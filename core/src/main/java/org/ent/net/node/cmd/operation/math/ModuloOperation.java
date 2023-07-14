package org.ent.net.node.cmd.operation.math;

import org.ent.net.AccessToken;
import org.ent.net.Purview;
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
    public ExecutionResult doApply(Node node1, Node node2, Node node3, AccessToken accessToken) {
        if (!node1.permittedToSetValue(accessToken)) {
            return ExecutionResult.ERROR;
        }
        int a = node2.getValue(Purview.COMMAND);
        int b = node3.getValue(Purview.COMMAND);
        if (b == 0) {
            return ExecutionResult.ERROR;
        }
        node1.setValue(compute(a, b));
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