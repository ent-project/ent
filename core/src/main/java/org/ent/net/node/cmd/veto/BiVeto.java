package org.ent.net.node.cmd.veto;

import org.ent.net.node.Node;
import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.permission.Permissions;

public class BiVeto implements Veto {

    private final Accessor accessor1;

    private final Accessor accessor2;

    private final BiCondition condition;

    private final boolean not;

    private final int value;
    private final int valueBase;

    private final String shortName;

    public BiVeto(Accessor accessor1, Accessor accessor2, BiCondition condition, boolean not) {
        this.accessor1 = accessor1;
        this.accessor2 = accessor2;
        this.condition = condition;
        this.not = not;
        this.valueBase = Veto.VETO_FLAG | (not ? 1 : 0) | (condition.getCode() << 1);
        this.value = valueBase | (accessor1.getCode() << 12) | (accessor2.getCode() << 16);
        this.shortName = buildShortName();
    }

    @Override
    public boolean evaluate(Node base, Permissions permissions) {
        Node node1 = accessor1.getTarget(base, permissions);
        Node node2 = accessor2.getTarget(base, permissions);
        return not ^ condition.evaluate(node1, node2, permissions);
    }

    private String buildShortName() {
        String accessor1Name = this.accessor1.getShortName();
        String accessor2Name = this.accessor2.getShortName();
        String conditionShortName = not ? this.condition.getInvertedShortName() : this.condition.getShortName();
        return "?" + accessor1Name + conditionShortName + accessor2Name + "?";
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public int getValueBase() {
        return valueBase;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    @Override
    public int getNumberOfParameters() {
        return 2;
    }
}
