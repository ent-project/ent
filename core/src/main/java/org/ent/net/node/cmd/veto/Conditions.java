package org.ent.net.node.cmd.veto;

public class Conditions {

    static final int CODE_IDENTICAL_CONDITION = 0b0;
    static final int CODE_SAME_VALUE_CONDITION = 0b1;
    static final int CODE_GREATER_THAN_CONDITION = 0b10;

    public static final IdenticalCondition IDENTICAL_CONDITION = new IdenticalCondition();
    public static final SameValueCondition SAME_VALUE_CONDITION = new SameValueCondition();
    public static final GreaterThanCondition GREATER_THAN_CONDITION = new GreaterThanCondition();

    private Conditions() {
    }
}
