package org.ent.dev.randnet;

import org.ent.net.node.cmd.ParameterizedValue;

public class PortalValue implements ParameterizedValue {
    private final int value;

    public PortalValue(int value) {
        this.value = value;
    }

    public PortalValue(int left, int right) {
        if (left < 0) {
            throw new IllegalArgumentException();
        }
        if (right < 0) {
            throw new IllegalArgumentException();
        }
        this.value = ((right ^ 0xFFFF) << 16) | (left ^ 0xFFFF);
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
