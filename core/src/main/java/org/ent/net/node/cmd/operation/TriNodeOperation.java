package org.ent.net.node.cmd.operation;

import org.ent.net.AccessToken;
import org.ent.net.Arrow;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public abstract class TriNodeOperation implements TriOperation {

    @Override
    public ExecutionResult apply(Arrow handle1, Arrow handle2, Arrow handle3, AccessToken accessToken) {
        return doApply(
                handle1.getTarget(Purview.COMMAND),
                handle2.getTarget(Purview.COMMAND),
                handle3.getTarget(Purview.COMMAND),
                accessToken);
    }

    public abstract ExecutionResult doApply(Node node1, Node node2, Node node3, AccessToken accessToken);
}
