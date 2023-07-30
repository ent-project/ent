package org.ent.net.node.cmd.operation;

import org.ent.Ent;
import org.ent.net.AccessToken;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public abstract class TriValueOperation extends TriNodeOperation {
    @Override
    public ExecutionResult doApply(Node node1, Node node2, Node node3, Ent ent, AccessToken accessToken) {
        if (!node1.permittedToSetValue(accessToken)) {
            return ExecutionResult.ERROR;
        }
        node1.setValue(compute(node2.getValue(Purview.COMMAND), node3.getValue(Purview.COMMAND)));
        ent.event().transverValue(node2, node1);
        ent.event().transverValue(node3, node1);
        ent.event().triValueOperation(node1, node2, node3, this);
        return ExecutionResult.NORMAL;
    }

    public abstract int compute(int a, int b);

    @Override
    public String getFirstSeparator() {
        return "=";
    }
}
