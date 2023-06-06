package org.ent.net.node.cmd.operation;

import org.ent.Ent;
import org.ent.net.Arrow;
import org.ent.net.AccessToken;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public abstract class MonoNodeOperation implements MonoOperation {
    @Override
    public ExecutionResult apply(Arrow arrowToNode, Ent ent, AccessToken accessToken) {
        Node node = arrowToNode.getTarget(Purview.COMMAND);
        return doApply(node, ent, accessToken);
    }

    protected abstract ExecutionResult doApply(Node node, Ent ent, AccessToken accessToken);

}
