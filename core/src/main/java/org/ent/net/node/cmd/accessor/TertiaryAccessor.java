package org.ent.net.node.cmd.accessor;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.node.Node;
import org.ent.permission.Permissions;

import static org.ent.net.ArrowDirection.ARROW_SYMBOLS;

public class TertiaryAccessor implements Accessor {

    private final ArrowDirection direction1;

    private final ArrowDirection direction2;

    private final ArrowDirection direction3;

    private final ArrowDirection[] path;

    private final int code;

    private final String shortName;

    public TertiaryAccessor(ArrowDirection direction1, ArrowDirection direction2, ArrowDirection direction3) {
        this.direction1 = direction1;
        this.direction2 = direction2;
        this.direction3 = direction3;
        this.path = new ArrowDirection[]{ArrowDirection.LEFT, direction1, direction2, direction3};
        this.code = 0b1000 | (direction1 == ArrowDirection.RIGHT ? 0b0001 : 0) | (direction2 == ArrowDirection.RIGHT ? 0b0010 : 0) | (direction3 == ArrowDirection.RIGHT ? 0b0100 : 0);
        this.shortName = ARROW_SYMBOLS.get(ArrowDirection.LEFT)
                + ARROW_SYMBOLS.get(direction1)
                + ARROW_SYMBOLS.get(direction2)
                + ARROW_SYMBOLS.get(direction3);
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public Arrow get(Node base, Permissions permissions) {
        Node node1 = base.getLeftChild(permissions);
        Arrow arrow1 = node1.getArrow(direction1);
        Node node2 = arrow1.getTarget(permissions);
        Arrow arrow2 = node2.getArrow(direction2);
        Node node3 = arrow2.getTarget(permissions);
        return node3.getArrow(direction3);
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    @Override
    public ArrowDirection[] getPath() {
        return path;
    }

    @Override
    public String toString() {
        return getShortName();
    }

}
