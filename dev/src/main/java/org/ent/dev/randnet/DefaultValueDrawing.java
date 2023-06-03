package org.ent.dev.randnet;

import org.ent.net.ArrowDirection;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.ParameterizedValue;
import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.accessor.Accessors;
import org.ent.net.node.cmd.accessor.PrimaryAccessor;
import org.ent.net.node.cmd.accessor.SecondaryAccessor;
import org.ent.net.node.cmd.accessor.TertiaryAccessor;
import org.ent.net.node.cmd.operation.BiOperation;
import org.ent.net.node.cmd.operation.MonoOperation;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.net.node.cmd.operation.TriOperation;
import org.ent.net.node.cmd.veto.BiCondition;
import org.ent.net.node.cmd.veto.Conditions;
import org.ent.net.node.cmd.veto.Vetos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DefaultValueDrawing implements ValueDrawing {

    public static final int WEIGHT1 = 10;
    public static final int WEIGHT2 = 4;
    public static final int WEIGHT3 = 3;
    private final List<ParameterizedValue> valueSamples = new ArrayList<>();
    private final List<Accessor> accessorSamples = new ArrayList<>();

    private final Random rand;

    public DefaultValueDrawing() {
        addValueBase(Commands.NOP, WEIGHT3);
        addValueBase(Operations.ANCESTOR_EXCHANGE_OPERATION, WEIGHT1);
        addValueBase(Operations.SET_OPERATION, WEIGHT1);
        addValueBase(Operations.DUP_OPERATION, WEIGHT1);
        addValueBase(Operations.SET_VALUE_OPERATION, WEIGHT1);
        addValueBase(Operations.EVAL_OPERATION, WEIGHT2);

        addValueBase(Operations.NEG_OPERATION, WEIGHT3);
        addValueBase(Operations.INC_OPERATION, WEIGHT2);
        addValueBase(Operations.DEC_OPERATION, WEIGHT2);
        addValueBase(Operations.PLUS_OPERATION, WEIGHT2);
        addValueBase(Operations.MINUS_OPERATION, WEIGHT3);
        addValueBase(Operations.MULTIPLY_OPERATION, WEIGHT2);
        addValueBase(Operations.MODULO_OPERATION, WEIGHT2);
        addValueBase(Operations.XOR_OPERATION, WEIGHT2);
        addValueBase(Operations.BITWISE_AND_OPERATION, WEIGHT2);
        addValueBase(Operations.BITWISE_OR_OPERATION, WEIGHT2);
        addValueBase(Operations.ROTATE_RIGHT_OPERATION, WEIGHT3);
        addValueBase(Operations.SHIFT_LEFT_OPERATION, WEIGHT3);
        addValueBase(Operations.SHIFT_RIGHT_OPERATION, WEIGHT3);

        addValueBase(Conditions.IDENTICAL_CONDITION, false, WEIGHT3);
        addValueBase(Conditions.IDENTICAL_CONDITION, true, WEIGHT3);
        addValueBase(Conditions.SAME_VALUE_CONDITION, false, WEIGHT3);
        addValueBase(Conditions.SAME_VALUE_CONDITION, true, WEIGHT3);
        addValueBase(Conditions.GREATER_THAN_CONDITION, false, WEIGHT3);
        addValueBase(Conditions.GREATER_THAN_CONDITION, true, WEIGHT3);

        addAccessor(Accessors.FLOW, 4);
        addAccessor(Accessors.DIRECT, 4);
        for (ArrowDirection direction1 : ArrowDirection.values()) {
            addAccessor(new PrimaryAccessor(direction1), 4);
        }
        for (ArrowDirection direction1 : ArrowDirection.values()) {
            for (ArrowDirection direction2 : ArrowDirection.values()) {
                addAccessor(new SecondaryAccessor(direction1, direction2), 2);
            }
        }
        for (ArrowDirection direction1 : ArrowDirection.values()) {
            for (ArrowDirection direction2 : ArrowDirection.values()) {
                for (ArrowDirection direction3 : ArrowDirection.values()) {
                    addAccessor(new TertiaryAccessor(direction1, direction2, direction3), 1);
                }
            }
        }
        if (accessorSamples.size() != 32) {
            throw new AssertionError();
        }

        rand = new Random(7);
    }

    @Override
    public int drawValue() {
        int valueDraw = rand.nextInt(valueSamples.size());
        ParameterizedValue pValue = valueSamples.get(valueDraw);
        int accessors = drawAccessors(pValue.getNumberOfParameters());
        return pValue.getValueBase() | accessors;
    }

    private int drawAccessors(int numberOfParameters) {
        int result = 0;
        if (numberOfParameters == 0) {
            return result;
        }
        int draw = rand.nextInt();
        int accIdx1 = draw & 0b11111;
        result |= accessorSamples.get(accIdx1).getCode() << 12;
        if (numberOfParameters == 1) {
            return result;
        }
        int accIdx2 = (draw >> 5) & 0b11111;
        result |= accessorSamples.get(accIdx2).getCode() << 16;
        if (numberOfParameters == 2) {
            return result;
        }
        int accIdx3 = (draw >> 10) & 0b11111;
        result |= accessorSamples.get(accIdx3).getCode() << 20;
        return result;
    }

    private void addAccessor(Accessor accessor, int weight) {
        for (int i = 0; i < weight; i++) {
            accessorSamples.add(accessor);
        }
    }

    private void addValueBase(MonoOperation operation, int weight) {
        addValueBase(getCommandBase(operation), weight);
    }

    private void addValueBase(BiCondition condition, boolean not, int weight) {
        for (int i = 0; i < weight; i++) {
            valueSamples.add(Vetos.get(condition, not, Accessors.FLOW, Accessors.FLOW));
        }
    }

    private void addValueBase(TriOperation operation, int weight) {
        addValueBase(getCommandBase(operation), weight);
    }

    private Command getCommandBase(MonoOperation operation) {
        return Commands.get(operation, Accessors.FLOW);
    }

    private Command getCommandBase(TriOperation operation) {
        return Commands.get(operation, Accessors.FLOW, Accessors.FLOW, Accessors.FLOW);
    }

    private void addValueBase(BiOperation operation, int weight) {
        addValueBase(getCommandBase(operation), weight);
    }

    private Command getCommandBase(BiOperation o) {
        return Commands.get(o, Accessors.FLOW, Accessors.FLOW);
    }

    private void addValueBase(Command command, int weight) {
        for (int i = 0; i < weight; i++) {
            valueSamples.add(command);
        }
    }
}
