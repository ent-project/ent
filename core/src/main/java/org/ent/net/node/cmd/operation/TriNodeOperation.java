package org.ent.net.node.cmd.operation;

import org.ent.permission.Permissions;
import org.ent.net.Arrow;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public abstract class TriNodeOperation implements TriOperation {

    @Override
    public ExecutionResult apply(Arrow handle1, Arrow handle2, Arrow handle3, Permissions permissions) {
        return doApply(
                handle1.getTarget(permissions),
                handle2.getTarget(permissions),
                handle3.getTarget(permissions),
                permissions);
    }

    public abstract ExecutionResult doApply(Node node1, Node node2, Node node3, Permissions permissions);
}
