package org.ent.dev.game.juniper;

import org.apache.commons.lang3.ArrayUtils;

enum Direction {
    E('e', 1, 0),
    S('s', 0, 1),
    W('w', -1, 0),
    N('n', 0, -1);

    private final char label;
    private final int deltaX;
    private final int deltaY;

    Direction(char label, int deltaX, int deltaY) {
        this.label = label;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    public int deltaX() {
        return deltaX;
    }

    public int deltaY() {
        return deltaY;
    }

    public char label() {
        return label;
    }

    public static Direction[] valuesReversed() {
        // FIXME: performance
        Direction[] values = values();
        ArrayUtils.reverse(values);
        return values;
    }

}
