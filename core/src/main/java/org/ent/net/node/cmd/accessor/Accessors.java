package org.ent.net.node.cmd.accessor;

import org.ent.net.ArrowDirection;

import java.util.ArrayList;
import java.util.List;

public class Accessors {
    /*
    Accessor overview:
                                 <> (root)
                                /  \
                       DIRECT /     \ FLOW
                            /        \
            ("parameters") o         <> (next root)
                         /  \
                 LEFT  /     \ RIGHT
                     /        \
   ("1st argument") o          o ("2nd argument")
                  /  \       /  \
              LL/   LR\    /RL   \RR
              o        o  o       o

         TertiaryAccessor goes one level deeper
     */

    public static final FlowAccessor FLOW = new FlowAccessor();
    public static final DirectAccessor DIRECT = new DirectAccessor();
    public static final PrimaryAccessor LEFT = new PrimaryAccessor(ArrowDirection.LEFT);
    public static final PrimaryAccessor RIGHT = new PrimaryAccessor(ArrowDirection.RIGHT);
    public static final SecondaryAccessor LEFT_LEFT = new SecondaryAccessor(ArrowDirection.LEFT, ArrowDirection.LEFT);
    public static final SecondaryAccessor LEFT_RIGHT = new SecondaryAccessor(ArrowDirection.LEFT, ArrowDirection.RIGHT);
    public static final SecondaryAccessor RIGHT_LEFT = new SecondaryAccessor(ArrowDirection.RIGHT, ArrowDirection.LEFT);
    public static final SecondaryAccessor RIGHT_RIGHT = new SecondaryAccessor(ArrowDirection.RIGHT, ArrowDirection.RIGHT);

    public static final TertiaryAccessor LEFT_LEFT_LEFT = new TertiaryAccessor(ArrowDirection.LEFT, ArrowDirection.LEFT, ArrowDirection.LEFT);
    public static final TertiaryAccessor LEFT_LEFT_RIGHT = new TertiaryAccessor(ArrowDirection.LEFT, ArrowDirection.LEFT, ArrowDirection.RIGHT);
    public static final TertiaryAccessor LEFT_RIGHT_LEFT = new TertiaryAccessor(ArrowDirection.LEFT, ArrowDirection.RIGHT, ArrowDirection.LEFT);

    public static final List<Accessor> ALL_ACCESSORS;

    private Accessors() {
    }

    static {
        ALL_ACCESSORS = new ArrayList<>();
        ALL_ACCESSORS.add(FLOW);
        ALL_ACCESSORS.add(DIRECT);
        ALL_ACCESSORS.add(LEFT);
        ALL_ACCESSORS.add(RIGHT);
        ALL_ACCESSORS.add(LEFT_LEFT);
        ALL_ACCESSORS.add(LEFT_RIGHT);
        ALL_ACCESSORS.add(RIGHT_LEFT);
        ALL_ACCESSORS.add(RIGHT_RIGHT);
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
            case LEFT -> LEFT;
            case RIGHT -> RIGHT;
        };
    }

    public static Accessor get(ArrowDirection direction1, ArrowDirection direction2) {
        return switch (direction1) {
            case LEFT -> switch (direction2) {
                case LEFT -> LEFT_LEFT;
                case RIGHT -> LEFT_RIGHT;
            };
            case RIGHT -> switch (direction2) {
                case LEFT -> RIGHT_LEFT;
                case RIGHT -> RIGHT_RIGHT;
            };
        };
    }

}
