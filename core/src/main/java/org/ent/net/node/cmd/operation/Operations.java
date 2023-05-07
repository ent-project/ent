package org.ent.net.node.cmd.operation;

public class Operations {

    public static final int CODE_SET_OPERATION = 0b1;
    public static final int CODE_ANCESTOR_EXCHANGE_OPERATION = 0b10;
    public static final int CODE_ANCESTOR_SWAP_OPERATION = 0b11;
    public static final int CODE_DUP_OPERATION = 0b100;
    public static final int CODE_DUP_REGULAR_OPERATION = 0b101;
    public static final int CODE_IS_IDENTICAL_OPERATION = 0b1100;
    public static final int CODE_ADD_OPERATION = 0b11100;

    public static final SetOperation SET = new SetOperation();
    public static final AncestorExchangeOperation ANCESTOR_EXCHANGE = new AncestorExchangeOperation();

    private Operations() {
    }
}
