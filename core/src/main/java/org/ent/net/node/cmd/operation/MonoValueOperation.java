package org.ent.net.node.cmd.operation;

import org.ent.Ent;
import org.ent.net.AccessToken;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public abstract class MonoValueOperation extends MonoNodeOperation {

    @Override
    public ExecutionResult doApply(Node node, Ent ent, AccessToken accessToken) {
        if (!node.permittedToSetValue(accessToken)) {
            return ExecutionResult.ERROR;
        }
        int newValue = compute(node.getValue());
        node.setValue(newValue);
        return ExecutionResult.NORMAL;
    }

    public abstract int compute(int a);
}