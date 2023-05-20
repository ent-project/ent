package org.ent.net.node.cmd.veto;

import org.ent.Ent;
import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Purview;
import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.accessor.PrimaryAccessor;

public class BiVeto implements Veto {

/*
value bit pattern:
|                 |                 |                 |                 |
  P V O O O O O O   Z Z Z Z Y Y Y Y   X X X X C C C C   C C C C C C C N

N - 'not' flag (inverts the condition)
C - condition code
X - accessor for the first parameter
Y - accessor for the second parameter
Z - accessor for the third parameter, all 0 in this case
O - unused = 0
V - veto flag = 1
P - portal flag = 0

 */

    private final Accessor accessor1;

    private final Accessor accessor2;

    private final BiCondition condition;

    private final boolean not;

    private final int value;

    private final String shortName;

    public BiVeto(Accessor accessor1, Accessor accessor2, BiCondition condition, boolean not) {
        this.accessor1 = accessor1;
        this.accessor2 = accessor2;
        this.condition = condition;
        this.not = not;
        this.value = Veto.VETO_FLAG |
                (not ? 1 : 0) |
                (condition.getCode() << 1) |
                (accessor1.getCode() << 12) |
                (accessor2.getCode() << 16);
        this.shortName = buildShortName();
    }

    @Override
    public boolean evaluate(Arrow parameters, Ent ent) {
        Arrow arrow1 = accessor1.get(parameters, ent, Purview.COMMAND);
        Arrow arrow2 = accessor2.get(parameters, ent, Purview.COMMAND);
        return not ^ condition.evaluate(arrow1, arrow2);
    }

    private String buildShortName() {
        String accessor1Name = this.accessor1.getShortName();
        if (this.accessor1 instanceof PrimaryAccessor primaryLeft && primaryLeft.getDirection() == ArrowDirection.LEFT) {
            accessor1Name = "";
        }
        String accessor2Name = this.accessor2.getShortName();
        if (this.accessor2 instanceof PrimaryAccessor primaryRight && primaryRight.getDirection() == ArrowDirection.RIGHT) {
            accessor2Name = "";
        }
        String conditionShortName = not ? this.condition.getInvertedShortName() : this.condition.getShortName();
        return "?" + accessor1Name + conditionShortName + accessor2Name + "?";
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getShortName() {
        return shortName;
    }
}
