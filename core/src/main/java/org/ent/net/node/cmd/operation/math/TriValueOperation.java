package org.ent.net.node.cmd.operation.math;

import org.ent.net.Arrow;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;
import org.ent.net.node.cmd.operation.TriOperation;

public abstract class TriValueOperation implements TriOperation {

    @Override
    public ExecutionResult apply(Arrow handle1, Arrow handle2, Arrow handle3) {
        Node node1 = handle1.getTarget(Purview.COMMAND);
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
