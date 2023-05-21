package org.ent.net.node.cmd.veto;

import org.ent.Ent;
import org.ent.net.ArrowDirection;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.accessor.PrimaryAccessor;

public class BiVeto implements Veto {

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
    public boolean evaluate(Node base, Ent ent) {
        Node node1 = accessor1.getTarget(base, ent, Purview.COMMAND);
        Node node2 = accessor2.getTarget(base, ent, Purview.COMMAND);
        boolean result = not ^ condition.evaluate(node1, node2);
        ent.event().vetoEvaluation(this, node1, node2, result);
        return result;
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
