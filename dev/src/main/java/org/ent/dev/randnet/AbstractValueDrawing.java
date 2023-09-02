package org.ent.dev.randnet;

import org.apache.commons.rng.UniformRandomProvider;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractValueDrawing implements ValueDrawing {

    public static final int WEIGHT1 = 10;
    public static final int WEIGHT2 = 4;
    public static final int WEIGHT3 = 3;
    protected int[] valueSamples_valueBase;
    protected byte[] valueSamples_numberOfParameters;
    private final List<Integer> accessorSamples_code = new ArrayList<>();

    public static class ValueCollector {

        private final Map<ParameterizedValue, Double> collector = new LinkedHashMap<>();
        private double totalWeight;

        private int[] valueBase;
        private byte[] numberOfParameters;

        public void addValueBase(ParameterizedValue value, double weight) {
            collector.put(value, weight);
            totalWeight += weight;
        }


        protected void addValueBase(MonoOperation operation, double weight) {
            addValueBase(getCommandBase(operation), weight);
        }

        protected void addValueBase(BiCondition condition, boolean not, double weight) {
            addValueBase(Vetos.get(condition, not, Accessors.FLOW, Accessors.FLOW), weight);
        }

        protected void addValueBase(TriOperation operation, double weight) {
            addValueBase(getCommandBase(operation), weight);
        }

        private Command getCommandBase(MonoOperation operation) {
            return Commands.get(operation, Accessors.FLOW);
        }

        private Command getCommandBase(TriOperation operation) {
            return Commands.get(operation, Accessors.FLOW, Accessors.FLOW, Accessors.FLOW);
        }

        protected void addValueBase(BiOperation operation, double weight) {
            addValueBase(getCommandBase(operation), weight);
        }

        private Command getCommandBase(BiOperation o) {
            return Commands.get(o, Accessors.FLOW, Accessors.FLOW);
        }

        public void buildValueSamples(int targetSize) {
            int discreteTotalWeight = 0;
            for (double weight : collector.values()) {
                int discreteWeight = (int) Math.round(weight * targetSize / totalWeight);
                discreteTotalWeight += discreteWeight;
            }

            valueBase = new int[discreteTotalWeight];
            numberOfParameters = new byte[discreteTotalWeight];
            int i = 0;
            for (Map.Entry<ParameterizedValue, Double> entry : collector.entrySet()) {
                ParameterizedValue value = entry.getKey();
                double weight = entry.getValue();
                int discreteWeight = (int) Math.round(weight * targetSize / totalWeight);
                for (int j = 0; j < discreteWeight; j++) {
                    // optimization: direct access for fields "valueBase" and "numberOfParameters" of ParameterizedValue
                    valueBase[i] = value.getValueBase();
                    numberOfParameters[i] = (byte) value.getNumberOfParameters();
                    i++;
                }
            }
        }

        public int[] getValueBase() {
            return valueBase;
        }

        public byte[] getNumberOfParameters() {
            return numberOfParameters;
        }
    }

    public AbstractValueDrawing() {
        initializeAccessors();
    }

    protected void defaultValueInitialization() {
        ValueCollector collector = new ValueCollector();
        initializeValues(collector);
        collector.buildValueSamples((int) collector.totalWeight);
        valueSamples_valueBase = collector.getValueBase();
        valueSamples_numberOfParameters = collector.getNumberOfParameters();
    }

    protected void initializeValues(ValueCollector collector) {
        // override
    }

    public void defaultInitializeValues(ValueCollector collector) {
        collector.addValueBase(Commands.NOP, WEIGHT3);
        collector.addValueBase(Operations.ANCESTOR_EXCHANGE_OPERATION, WEIGHT1);
        collector.addValueBase(Operations.SET_OPERATION, WEIGHT1);
        collector.addValueBase(Operations.DUP_OPERATION, WEIGHT1);
        collector.addValueBase(Operations.SET_VALUE_OPERATION, WEIGHT1);
        collector.addValueBase(Operations.EVAL_OPERATION, WEIGHT2);
        collector.addValueBase(Operations.EVAL_FLOW_OPERATION, WEIGHT2);

        collector.addValueBase(Operations.NEG_OPERATION, WEIGHT3);
        collector.addValueBase(Operations.INC_OPERATION, WEIGHT2);
        collector.addValueBase(Operations.DEC_OPERATION, WEIGHT2);
        collector.addValueBase(Operations.PLUS_OPERATION, WEIGHT2);
        collector.addValueBase(Operations.MINUS_OPERATION, WEIGHT3);
        collector.addValueBase(Operations.MULTIPLY_OPERATION, WEIGHT2);
        collector.addValueBase(Operations.MODULO_OPERATION, WEIGHT2);
        collector.addValueBase(Operations.XOR_OPERATION, WEIGHT2);
        collector.addValueBase(Operations.BITWISE_AND_OPERATION, WEIGHT2);
        collector.addValueBase(Operations.BITWISE_OR_OPERATION, WEIGHT2);
        collector.addValueBase(Operations.ROTATE_RIGHT_OPERATION, WEIGHT3);
        collector.addValueBase(Operations.SHIFT_LEFT_OPERATION, WEIGHT3);
        collector.addValueBase(Operations.SHIFT_RIGHT_OPERATION, WEIGHT3);

        collector.addValueBase(Conditions.IDENTICAL_CONDITION, false, WEIGHT3);
        collector.addValueBase(Conditions.IDENTICAL_CONDITION, true, WEIGHT3);
        collector.addValueBase(Conditions.SAME_VALUE_CONDITION, false, WEIGHT3);
        collector.addValueBase(Conditions.SAME_VALUE_CONDITION, true, WEIGHT3);
        collector.addValueBase(Conditions.GREATER_THAN_CONDITION, false, WEIGHT3);
        collector.addValueBase(Conditions.GREATER_THAN_CONDITION, true, WEIGHT3);
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

    public int drawValue(UniformRandomProvider rand) {
        int valueIndex = rand.nextInt(valueSamples_valueBase.length);
        int accessors = drawAccessors(valueSamples_numberOfParameters[valueIndex], rand);
        return valueSamples_valueBase[valueIndex] | accessors;
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
}
