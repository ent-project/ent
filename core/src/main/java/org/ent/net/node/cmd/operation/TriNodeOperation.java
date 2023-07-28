package org.ent.net.node.cmd.operation;

import org.ent.Ent;
import org.ent.net.AccessToken;
import org.ent.net.Arrow;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public abstract class TriNodeOperation implements TriOperation {

    @Override
    public ExecutionResult apply(Arrow handle1, Arrow handle2, Arrow handle3, Ent ent, AccessToken accessToken) {
        return doApply(
                handle1.getTarget(Purview.COMMAND),
                handle2.getTarget(Purview.COMMAND),
                handle3.getTarget(Purview.COMMAND),
                ent, accessToken);
    }

    public abstract ExecutionResult doApply(Node node1, Node node2, Node node3, Ent ent, AccessToken accessToken);
}
