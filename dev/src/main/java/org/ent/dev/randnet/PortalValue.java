package org.ent.dev.randnet;

import org.ent.net.node.cmd.ParameterizedValue;

public class PortalValue implements ParameterizedValue {
    private final int value;

    public PortalValue(int value) {
        this.value = value;
    }

    public PortalValue(Integer left, Integer right) {
        int value = 0;
        if (left != null) {
            if (left < 0) {
                throw new IllegalArgumentException();
            }
            value = left ^ 0xFFFF;
        }
        if (right != null) {
            if (right < 0) {
                throw new IllegalArgumentException();
            }
            value |= ((right ^ 0xFFFF) << 16);
        }
        this.value = value;
    }

    @Override
    public int getValueBase() {
        return value;
    }

    @Override
    public int getNumberOfParameters() {
        return 0;
    }
}
