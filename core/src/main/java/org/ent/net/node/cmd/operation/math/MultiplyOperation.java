package org.ent.net.node.cmd.operation.math;

import org.ent.net.node.cmd.operation.Operations;

public class MultiplyOperation extends TriValueOperation {
    @Override
    public int getCode() {
        return Operations.CODE_MULTIPLY_OPERATION;
    }

    @Override
    public int compute(int a, int b) {
        return a * b;
    }

    @Override
    public String getSecondSeparator() {
        return "m";
    }
}
