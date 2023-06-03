package org.ent.net.node.cmd.operation;

import org.ent.Ent;
import org.ent.net.Arrow;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public abstract class MonoNodeOperation implements MonoOperation {
    @Override
    public ExecutionResult apply(Arrow arrowToNode, Ent ent) {
        Node node = arrowToNode.getTarget(Purview.COMMAND);
        node = ent.relayToOtherDomain(node);
        return doApply(node, ent);
    }

    protected abstract ExecutionResult doApply(Node node, Ent ent);

}
