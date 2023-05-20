package org.ent.net.node.cmd.operation;

import org.ent.net.node.cmd.operation.math.BitwiseAndOperation;
import org.ent.net.node.cmd.operation.math.BitwiseOrOperation;
import org.ent.net.node.cmd.operation.math.DecOperation;
import org.ent.net.node.cmd.operation.math.IncOperation;
import org.ent.net.node.cmd.operation.math.MinusOperation;
import org.ent.net.node.cmd.operation.math.ModuloOperation;
import org.ent.net.node.cmd.operation.math.MultiplyOperation;
import org.ent.net.node.cmd.operation.math.NegOperation;
import org.ent.net.node.cmd.operation.math.PlusOperation;
import org.ent.net.node.cmd.operation.math.RotateRightOperation;
import org.ent.net.node.cmd.operation.math.ShiftLeftOperation;
import org.ent.net.node.cmd.operation.math.ShiftRightOperation;
import org.ent.net.node.cmd.operation.math.XorOperation;

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

    public static final int CODE_SET_VALUE_OPERATION = 0b100000;
    public static final int CODE_NEG_OPERATION = 0b100001;
    public static final int CODE_INC_OPERATION = 0b100010;
    public static final int CODE_DEC_OPERATION = 0b100011;

    public static final SetOperation SET_OPERATION = new SetOperation();
    public static final AncestorExchangeOperation ANCESTOR_EXCHANGE_OPERATION = new AncestorExchangeOperation();
    public static final DupOperation DUP_OPERATION = new DupOperation();
    public static final AncestorExchangeNormalOperation ANCESTOR_EXCHANGE_NORMAL_OPERATION = new AncestorExchangeNormalOperation();
    public static final DupNormalOperation DUP_NORMAL_OPERATION = new DupNormalOperation();
    public static final IsIdenticalOperation IS_IDENTICAL_OPERATION = new IsIdenticalOperation();
    public static final SetValueOperation SET_VALUE_OPERATION = new SetValueOperation();
    public static final NegOperation NEG_OPERATION = new NegOperation();
    public static final IncOperation INC_OPERATION = new IncOperation();
    public static final DecOperation DEC_OPERATION = new DecOperation();
    public static final PlusOperation PLUS_OPERATION = new PlusOperation();
    public static final MinusOperation MINUS_OPERATION = new MinusOperation();
    public static final MultiplyOperation MULTIPLY_OPERATION = new MultiplyOperation();
    public static final ModuloOperation MODULO_OPERATION = new ModuloOperation();
    public static final XorOperation XOR_OPERATION = new XorOperation();
    public static final BitwiseAndOperation BITWISE_AND_OPERATION = new BitwiseAndOperation();
    public static final BitwiseOrOperation BITWISE_OR_OPERATION = new BitwiseOrOperation();
    public static final RotateRightOperation ROTATE_RIGHT_OPERATION = new RotateRightOperation();
    public static final ShiftLeftOperation SHIFT_LEFT_OPERATION = new ShiftLeftOperation();
    public static final ShiftRightOperation SHIFT_RIGHT_OPERATION = new ShiftRightOperation();

    private Operations() {
    }
}
