package org.ent.net.node.cmd.accessor;

import org.ent.net.ArrowDirection;

public class Accessors {

    public static final DirectAccessor DIRECT = new DirectAccessor();
    public static final PrimaryAccessor LEFT = new PrimaryAccessor(ArrowDirection.LEFT);
    public static final PrimaryAccessor RIGHT = new PrimaryAccessor(ArrowDirection.RIGHT);
    public static final SecondaryAccessor LEFT_LEFT = new SecondaryAccessor(ArrowDirection.LEFT, ArrowDirection.LEFT);
    public static final SecondaryAccessor LEFT_RIGHT = new SecondaryAccessor(ArrowDirection.LEFT, ArrowDirection.RIGHT);
    public static final SecondaryAccessor RIGHT_LEFT = new SecondaryAccessor(ArrowDirection.RIGHT, ArrowDirection.LEFT);
    public static final SecondaryAccessor RIGHT_RIGHT = new SecondaryAccessor(ArrowDirection.RIGHT, ArrowDirection.RIGHT);

    private Accessors() {
    }
}
