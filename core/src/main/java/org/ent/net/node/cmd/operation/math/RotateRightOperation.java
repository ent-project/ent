package org.ent.net.node.cmd.operation.math;

import org.ent.net.node.cmd.operation.Operations;
import org.ent.net.node.cmd.operation.TriValueOperation;

public class RotateRightOperation extends TriValueOperation {
    @Override
    public int getCode() {
        return Operations.CODE_ROTATE_RIGHT_OPERATION;
    }

    @Override
    public int compute(int a, int b) {
        return Integer.rotateRight(a, b);
    }

    @Override
    public String getSecondSeparator() {
        return "rot";
    }
}
