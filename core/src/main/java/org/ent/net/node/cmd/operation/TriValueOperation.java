package org.ent.net.node.cmd.operation;

import org.ent.net.Arrow;
import org.ent.net.AccessToken;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public abstract class TriValueOperation implements TriOperation {

    @Override
    public ExecutionResult apply(Arrow handle1, Arrow handle2, Arrow handle3, AccessToken accessToken) {
        Node node1 = handle1.getTarget(Purview.COMMAND);
        if (!node1.permittedToSetValue(accessToken)) {
            return ExecutionResult.ERROR;
        }
        Node node2 = handle2.getTarget(Purview.COMMAND);
        Node node3 = handle3.getTarget(Purview.COMMAND);
        node1.setValue(compute(node2.getValue(), node3.getValue()));
        return ExecutionResult.NORMAL;
    }

    public abstract int compute(int a, int b);

    @Override
    public String getFirstSeparator() {
        return "=";
    }
}
