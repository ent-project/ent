package org.ent.net.node.cmd.operation.math;

import org.ent.net.node.cmd.operation.Operations;
import org.ent.net.node.cmd.operation.TriValueOperation;

public class BitwiseAndOperation extends TriValueOperation {
    @Override
    public int getCode() {
        return Operations.CODE_BITWISE_AND_OPERATION;
    }

    @Override
    public int compute(int a, int b) {
        return a & b;
    }

    @Override
    public String getSecondSeparator() {
        return "&";
    }
}
