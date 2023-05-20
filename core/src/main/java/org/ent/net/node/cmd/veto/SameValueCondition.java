package org.ent.net.node.cmd.veto;

public final class SameValueCondition extends BiValueCondition {

    @Override
    public int getCode() {
        return Conditions.CODE_SAME_VALUE_CONDITION;
    }

    @Override
    protected boolean test(int value1, int value2) {
        return value1 == value2;
    }

    @Override
    public String getShortName() {
        return "==";
    }

    @Override
    public String getInvertedShortName() {
        return "!=";
    }
}
