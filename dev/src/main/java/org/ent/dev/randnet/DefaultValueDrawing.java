package org.ent.dev.randnet;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.net.ArrowDirection;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.ParameterizedValue;
import org.ent.net.node.cmd.accessor.*;
import org.ent.net.node.cmd.operation.BiOperation;
import org.ent.net.node.cmd.operation.MonoOperation;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.net.node.cmd.operation.TriOperation;
import org.ent.net.node.cmd.veto.BiCondition;
import org.ent.net.node.cmd.veto.Conditions;
import org.ent.net.node.cmd.veto.Vetos;

import java.util.ArrayList;
import java.util.List;

public class DefaultValueDrawing {

    public static final int WEIGHT1 = 10;
    public static final int WEIGHT2 = 4;
    public static final int WEIGHT3 = 3;
    private final List<Integer> valueSamples_valueBase = new ArrayList<>();
    private final List<Integer> valueSamples_numberOfParameters = new ArrayList<>();
    private final List<Integer> accessorSamples_code = new ArrayList<>();

    public DefaultValueDrawing() {
        initializeValues();
        initializeAccessors();
    }

    protected void initializeValues() {
        addValueBase(Commands.NOP, WEIGHT3);
        addValueBase(Operations.ANCESTOR_EXCHANGE_OPERATION, WEIGHT1);
        addValueBase(Operations.SET_OPERATION, WEIGHT1);
        addValueBase(Operations.DUP_OPERATION, WEIGHT1);
        addValueBase(Operations.SET_VALUE_OPERATION, WEIGHT1);
        addValueBase(Operations.EVAL_OPERATION, WEIGHT2);
        addValueBase(Operations.EVAL_FLOW_OPERATION, WEIGHT2);

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
    }

    protected void initializeAccessors() {
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
        if (accessorSamples_code.size() != 32) {
            throw new AssertionError();
        }
    }

    public void addValueBase(ParameterizedValue value, int weight) {
        for (int i = 0; i < weight; i++) {
            // optimization: direct access for fields "valueBase" and "numberOfParameters" of ParameterizedValue
            valueSamples_valueBase.add(value.getValueBase());
            valueSamples_numberOfParameters.add(value.getNumberOfParameters());
        }
    }

    public int drawValue(UniformRandomProvider rand) {
        int valueIndex = rand.nextInt(valueSamples_valueBase.size());
        int accessors = drawAccessors(valueSamples_numberOfParameters.get(valueIndex), rand);
        return valueSamples_valueBase.get(valueIndex) | accessors;
    }

    private int drawAccessors(int numberOfParameters, UniformRandomProvider rand) {
        int result = 0;
        if (numberOfParameters == 0) {
            return result;
        }
        int draw = rand.nextInt();
        int accIdx1 = draw & 0b11111;
        result |= accessorSamples_code.get(accIdx1) << 12;
        if (numberOfParameters == 1) {
            return result;
        }
        int accIdx2 = (draw >> 5) & 0b11111;
        result |= accessorSamples_code.get(accIdx2) << 16;
        if (numberOfParameters == 2) {
            return result;
        }
        int accIdx3 = (draw >> 10) & 0b11111;
        result |= accessorSamples_code.get(accIdx3) << 20;
        return result;
    }

    private void addAccessor(Accessor accessor, int weight) {
        for (int i = 0; i < weight; i++) {
            // optimization: direct access for field "code" of Accessor
            accessorSamples_code.add(accessor.getCode());
        }
    }

    private void addValueBase(MonoOperation operation, int weight) {
        addValueBase(getCommandBase(operation), weight);
    }

    private void addValueBase(BiCondition condition, boolean not, int weight) {
        addValueBase(Vetos.get(condition, not, Accessors.FLOW, Accessors.FLOW), weight);
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

    protected void addValueBase(BiOperation operation, int weight) {
        addValueBase(getCommandBase(operation), weight);
    }

    private Command getCommandBase(BiOperation o) {
        return Commands.get(o, Accessors.FLOW, Accessors.FLOW);
    }
}
