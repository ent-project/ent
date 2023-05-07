package org.ent.net.node.cmd.operation;

import org.ent.net.node.cmd.operation.math.PlusOperation;

public class Operations {

    public static final int CODE_SET_OPERATION = 0b1;
    public static final int CODE_ANCESTOR_EXCHANGE_OPERATION = 0b10;
    public static final int CODE_ANCESTOR_SWAP_OPERATION = 0b11;
    public static final int CODE_DUP_OPERATION = 0b100;
    public static final int CODE_DUP_REGULAR_OPERATION = 0b101;
    public static final int CODE_IS_IDENTICAL_OPERATION = 0b1100;
    public static final int CODE_ADD_OPERATION = 0b10000;
    public static final int CODE_MINUS_OPERATION = 0b10001;
    public static final int CODE_MULTIPLY_OPERATION = 0b10010;
    public static final int CODE_MODULO_OPERATION = 0b10011;
    public static final int CODE_XOR_OPERATION = 0b10100;
    public static final int CODE_BITWISE_AND_OPERATION = 0b10101;
    public static final int CODE_BITWISE_OR_OPERATION = 0b10110;
    public static final int CODE_ROTATE_RIGHT_OPERATION = 0b10111;
    public static final int CODE_SHIFT_LEFT_OPERATION = 0b11000;
    public static final int CODE_SHIFT_RIGHT_OPERATION = 0b11001;

    public static final SetOperation SET = new SetOperation();
    public static final AncestorExchangeOperation ANCESTOR_EXCHANGE = new AncestorExchangeOperation();
    public static final PlusOperation PLUS = new PlusOperation();

    private Operations() {
    }
}
