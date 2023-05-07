package org.ent.net.node.cmd.accessor;

import org.ent.net.ArrowDirection;

public class Accessors {

    public static final NodeAccessor DIRECT = new NodeAccessor();
    public static final ArrowAccessor LEFT = new ArrowAccessor(ArrowDirection.LEFT);
    public static final ArrowAccessor RIGHT = new ArrowAccessor(ArrowDirection.RIGHT);
    public static final PtrArrowAccessor LEFT_LEFT = new PtrArrowAccessor(ArrowDirection.LEFT, ArrowDirection.LEFT);
    public static final PtrArrowAccessor LEFT_RIGHT = new PtrArrowAccessor(ArrowDirection.LEFT, ArrowDirection.RIGHT);
    public static final PtrArrowAccessor RIGHT_LEFT = new PtrArrowAccessor(ArrowDirection.RIGHT, ArrowDirection.LEFT);
    public static final PtrArrowAccessor RIGHT_RIGHT = new PtrArrowAccessor(ArrowDirection.RIGHT, ArrowDirection.RIGHT);

    private Accessors() {
    }
}
