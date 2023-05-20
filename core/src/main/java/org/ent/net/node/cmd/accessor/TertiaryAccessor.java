package org.ent.net.node.cmd.accessor;

import org.ent.Ent;
import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Purview;
import org.ent.net.node.Node;

public class TertiaryAccessor implements Accessor {

    private final ArrowDirection direction1;

    private final ArrowDirection direction2;

    private final ArrowDirection direction3;

    private final int code;

    private final String shortName;

    public TertiaryAccessor(ArrowDirection direction1, ArrowDirection direction2, ArrowDirection direction3) {
        this.direction1 = direction1;
        this.direction2 = direction2;
        this.direction3 = direction3;
        this.code = 0b1000 | (direction1 == ArrowDirection.RIGHT ? 0b0001 : 0) | (direction2 == ArrowDirection.RIGHT ? 0b0010 : 0) | (direction3 == ArrowDirection.RIGHT ? 0b0100 : 0);
        this.shortName = ArrowDirection.ARROW_SYMBOLS.get(direction1)
                + ArrowDirection.ARROW_SYMBOLS.get(direction2)
                + ArrowDirection.ARROW_SYMBOLS.get(direction3);
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public Arrow get(Node base, Ent ent, Purview purview) {
        Arrow arrow1 = ent.advanceWithPortals(base.getLeftChild(purview), direction1);
        Arrow arrow2 = ent.advanceWithPortals(arrow1.getTarget(purview), direction2);
        return ent.advanceWithPortals(arrow2.getTarget(purview), direction3);
    }

    @Override
    public String getShortName() {
        return shortName;
    }
}
