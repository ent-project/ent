package org.ent.net.node.cmd.veto;

public final class GreaterThanCondition extends BiValueCondition {

    @Override
    protected boolean test(int value1, int value2) {
        return value1 > value2;
    }

    @Override
    public int getCode() {
        return Conditions.CODE_GREATER_THAN_CONDITION;
    }

    @Override
    public String getShortName() {
        return "gt";
    }

    @Override
    public String getInvertedShortName() {
        return "<=";
    }
}
