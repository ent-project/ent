package org.ent.net.node.cmd.operation.math;

import org.ent.net.node.cmd.operation.Operations;

public class XorOperation extends TriValueOperation {
    @Override
    public int getCode() {
        return Operations.CODE_XOR_OPERATION;
    }

    @Override
    public int compute(int a, int b) {
        return a ^ b;
    }

    @Override
    public String getSecondSeparator() {
        return "^";
    }
}
