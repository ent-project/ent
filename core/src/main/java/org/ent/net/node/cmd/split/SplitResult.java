package org.ent.net.node.cmd.split;

import org.ent.net.ArrowDirection;

public enum SplitResult {
    NORMAL_LEFT(ArrowDirection.LEFT),
    NORMAL_RIGHT(ArrowDirection.RIGHT),
    ERROR(ArrowDirection.LEFT);

    private ArrowDirection direction;

    SplitResult(ArrowDirection direction) {
        this.direction = direction;
    }

    public ArrowDirection getDirection() {
        return direction;
    }
}
