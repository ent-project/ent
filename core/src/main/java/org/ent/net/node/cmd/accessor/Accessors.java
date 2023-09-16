package org.ent.net.node.cmd.accessor;

import org.ent.net.ArrowDirection;

import java.util.ArrayList;
import java.util.List;

public class Accessors {
    /*
    Accessor overview:
                                 <> (root)
                                /  \
                            L /     \ R
                            /        \
            ("parameters") o         <> (next root)
                         /  \
                    LL /     \ LR
                     /        \
   ("1st argument") o          o ("2nd argument")
                  /  \       /  \
             LLL/  LLR\    /LRL  \LRR
              o        o  o       o

         TertiaryAccessor goes one level deeper
     */

    public static final FlowAccessor R = new FlowAccessor();
    public static final DirectAccessor L = new DirectAccessor();
    public static final PrimaryAccessor LL = new PrimaryAccessor(ArrowDirection.LEFT);
    public static final PrimaryAccessor LR = new PrimaryAccessor(ArrowDirection.RIGHT);
    public static final SecondaryAccessor LLL = new SecondaryAccessor(ArrowDirection.LEFT, ArrowDirection.LEFT);
    public static final SecondaryAccessor LLR = new SecondaryAccessor(ArrowDirection.LEFT, ArrowDirection.RIGHT);
    public static final SecondaryAccessor LRL = new SecondaryAccessor(ArrowDirection.RIGHT, ArrowDirection.LEFT);
    public static final SecondaryAccessor LRR = new SecondaryAccessor(ArrowDirection.RIGHT, ArrowDirection.RIGHT);

    public static final TertiaryAccessor LLLL = new TertiaryAccessor(ArrowDirection.LEFT, ArrowDirection.LEFT, ArrowDirection.LEFT);
    public static final TertiaryAccessor LLLR = new TertiaryAccessor(ArrowDirection.LEFT, ArrowDirection.LEFT, ArrowDirection.RIGHT);
    public static final TertiaryAccessor LLRL = new TertiaryAccessor(ArrowDirection.LEFT, ArrowDirection.RIGHT, ArrowDirection.LEFT);
    public static final TertiaryAccessor LRLR = new TertiaryAccessor(ArrowDirection.RIGHT, ArrowDirection.LEFT, ArrowDirection.RIGHT);

    public static final List<Accessor> ALL_ACCESSORS;

    private Accessors() {
    }

    static {
        ALL_ACCESSORS = new ArrayList<>();
        ALL_ACCESSORS.add(R);
        ALL_ACCESSORS.add(L);
        ALL_ACCESSORS.add(LL);
        ALL_ACCESSORS.add(LR);
        ALL_ACCESSORS.add(LLL);
        ALL_ACCESSORS.add(LLR);
        ALL_ACCESSORS.add(LRL);
        ALL_ACCESSORS.add(LRR);
        for (ArrowDirection direction1 : ArrowDirection.values()) {
            for (ArrowDirection direction2 : ArrowDirection.values()) {
                for (ArrowDirection direction3 : ArrowDirection.values()) {
                    ALL_ACCESSORS.add(new TertiaryAccessor(direction1, direction2, direction3));
                }
            }
        }
    }

    public static Accessor get(ArrowDirection direction) {
        return switch (direction) {
            case LEFT -> LL;
            case RIGHT -> LR;
        };
    }

    public static Accessor get(ArrowDirection direction1, ArrowDirection direction2) {
        return switch (direction1) {
            case LEFT -> switch (direction2) {
                case LEFT -> LLL;
                case RIGHT -> LLR;
            };
            case RIGHT -> switch (direction2) {
                case LEFT -> LRL;
                case RIGHT -> LRR;
            };
        };
    }

}
