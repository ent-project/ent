package org.ent.net.node.cmd.operation;

import org.ent.permission.Permissions;
import org.ent.net.Arrow;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public abstract class MonoNodeOperation implements MonoOperation {
    @Override
    public ExecutionResult apply(Arrow arrowToNode, Permissions permissions) {
        Node node = arrowToNode.getTarget(permissions);
        return doApply(node, permissions);
    }

    protected abstract ExecutionResult doApply(Node node, Permissions permissions);

}
