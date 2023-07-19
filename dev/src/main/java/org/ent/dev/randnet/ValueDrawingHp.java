package org.ent.dev.randnet;

import org.ent.hyper.HyperManager;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.ParameterizedValue;
import org.ent.net.node.cmd.accessor.Accessors;
import org.ent.net.node.cmd.operation.BiOperation;
import org.ent.net.node.cmd.operation.MonoOperation;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.net.node.cmd.operation.TriOperation;
import org.ent.net.node.cmd.veto.BiCondition;
import org.ent.net.node.cmd.veto.Conditions;
import org.ent.net.node.cmd.veto.Vetos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class ValueDrawingHp extends DefaultValueDrawing2 {
    private static final Logger log = LoggerFactory.getLogger(ValueDrawingHp.class);

    protected final HyperManager hyperManager;

    public interface DistributionNode {
        void collect(double currentFractionBase, Map<ParameterizedValue, Double> values);
    }

    public static class DistributionSplit implements DistributionNode {
        final double fractionFirst;
        DistributionNode first, second;

        public DistributionSplit(double fractionFirst) {
            this.fractionFirst = fractionFirst;
        }

        public DistributionSplit first(DistributionNode first) {
            this.first = first;
            return this;
        }

        public DistributionSplit rest(DistributionNode second) {
            this.second = second;
            return this;
        }

        @Override
        public void collect(double currentFractionBase, Map<ParameterizedValue, Double> values) {
            first.collect(currentFractionBase * fractionFirst, values);
            second.collect(currentFractionBase * (1 - fractionFirst), values);
        }
    }

    public static class DistributionLeaf implements DistributionNode {
        Map<ParameterizedValue, Double> leafGroup = new LinkedHashMap<>();
        double totalWeight;

        public DistributionLeaf add(ParameterizedValue value, double groupWeight) {
            leafGroup.put(value, groupWeight);
            totalWeight += groupWeight;
            return this;
        }

        public DistributionLeaf add(MonoOperation operation, double groupWeight) {
            add(getCommandBase(operation), groupWeight);
            return this;
        }

        public DistributionLeaf add(BiOperation operation, double groupWeight) {
            add(getCommandBase(operation), groupWeight);
            return this;
        }

        public DistributionLeaf add(TriOperation operation, double groupWeight) {
            add(getCommandBase(operation), groupWeight);
            return this;
        }

        public DistributionLeaf add(BiCondition condition, boolean not, double groupWeight) {
            add(Vetos.get(condition, not, Accessors.FLOW, Accessors.FLOW), groupWeight);
            return this;
        }

        private Command getCommandBase(MonoOperation operation) {
            return Commands.get(operation, Accessors.FLOW);
        }

        private Command getCommandBase(BiOperation o) {
            return Commands.get(o, Accessors.FLOW, Accessors.FLOW);
        }

        private Command getCommandBase(TriOperation operation) {
            return Commands.get(operation, Accessors.FLOW, Accessors.FLOW, Accessors.FLOW);
        }

        @Override
        public void collect(double currentFractionBase, Map<ParameterizedValue, Double> values) {
            for (Map.Entry<ParameterizedValue, Double> entry : leafGroup.entrySet()) {
                ParameterizedValue value = entry.getKey();
                Double weightInGroup = entry.getValue();
                values.put(value, currentFractionBase * weightInGroup / totalWeight);
            }
        }
    }

    public ValueDrawingHp(HyperManager hyperManager) {
        this.hyperManager = hyperManager;
        DistributionNode distribution = initializeDistribution();
        if (hyperManager.isCollecting()) {
            return;
        }
        initialize(distribution, 50_000);
    }

    protected DistributionNode initializeDistribution() {
        double fracCommands = hyperManager.getDouble("fraction_commands", 0.0f, 1.0f);
        double fracMajorCommands = hyperManager.getDouble("fraction_major_commands", 0.0f, 1.0f);
        double fracMajorSplit = hyperManager.getDouble("fraction_major_split", 0.0f, 1.0f);
        double fracSet = hyperManager.getDouble("fraction_set", 0.0f, 1.0f);
        if (hyperManager.isCollecting()) {
            return null;
        }
        log.info("got HPs: fracCommands={}, fracMajorCommands={}, fracMajorSplit={}, fracSet={}", fracCommands, fracMajorCommands, fracMajorSplit, fracSet);

        return new DistributionSplit(fracCommands)
                // commands
                .first(new DistributionSplit(fracMajorCommands)
                        // major commands
                        .first(new DistributionSplit(fracMajorSplit)
                                // top 2 commands
                                .first(new DistributionSplit(fracSet)
                                        .first(new DistributionLeaf().add(Operations.SET_OPERATION, WEIGHT1))
                                        .rest(new DistributionLeaf().add(Operations.SET_VALUE_OPERATION, WEIGHT1))
                                )
                                // other major commands
                                .rest(new DistributionLeaf()
                                        .add(Commands.NOP, WEIGHT3)
                                        .add(Operations.ANCESTOR_EXCHANGE_OPERATION, WEIGHT1)
                                        .add(Operations.DUP_OPERATION, WEIGHT1)
                                        .add(Operations.EVAL_OPERATION, WEIGHT2)
                                        .add(Operations.EVAL_FLOW_OPERATION, WEIGHT2)
                                ))
                        // minor commands
                        .rest(new DistributionLeaf()
                                .add(Operations.NEG_OPERATION, WEIGHT3)
                                .add(Operations.INC_OPERATION, WEIGHT2)
                                .add(Operations.DEC_OPERATION, WEIGHT2)
                                .add(Operations.PLUS_OPERATION, WEIGHT2)
                                .add(Operations.MINUS_OPERATION, WEIGHT3)
                                .add(Operations.MULTIPLY_OPERATION, WEIGHT2)
                                .add(Operations.MODULO_OPERATION, WEIGHT2)
                                .add(Operations.XOR_OPERATION, WEIGHT2)
                                .add(Operations.BITWISE_AND_OPERATION, WEIGHT2)
                                .add(Operations.BITWISE_OR_OPERATION, WEIGHT2)
                                .add(Operations.ROTATE_RIGHT_OPERATION, WEIGHT3)
                                .add(Operations.SHIFT_LEFT_OPERATION, WEIGHT3)
                                .add(Operations.SHIFT_RIGHT_OPERATION, WEIGHT3)))
                // conditions
                .rest(new DistributionLeaf()
                        .add(Conditions.IDENTICAL_CONDITION, false, WEIGHT3)
                        .add(Conditions.IDENTICAL_CONDITION, true, WEIGHT3)
                        .add(Conditions.SAME_VALUE_CONDITION, false, WEIGHT3)
                        .add(Conditions.SAME_VALUE_CONDITION, true, WEIGHT3)
                        .add(Conditions.GREATER_THAN_CONDITION, false, WEIGHT3)
                        .add(Conditions.GREATER_THAN_CONDITION, true, WEIGHT3));
    }

    private void initialize(DistributionNode distribution, int targetSize) {
        LinkedHashMap<ParameterizedValue, Double> relativeWeights = new LinkedHashMap<>();
        distribution.collect(1.0, relativeWeights);

        double totalRelativeWeight = 0;
        for (Double relativeWeight : relativeWeights.values()) {
            totalRelativeWeight += relativeWeight;
        }
        // totalRelativeWeight should be ~ 1.0

        int discreteTotalWeight = 0;
        for (double weight : relativeWeights.values()) {
            int discreteWeight = (int) Math.round(weight * targetSize / totalRelativeWeight);
            discreteTotalWeight += discreteWeight;
        }

        valueSamples_valueBase = new int[discreteTotalWeight];
        valueSamples_numberOfParameters = new byte[discreteTotalWeight];
        int i = 0;
        for (Map.Entry<ParameterizedValue, Double> entry : relativeWeights.entrySet()) {
            ParameterizedValue value = entry.getKey();
            double weight = entry.getValue();
            int discreteWeight = (int) Math.round(weight * targetSize / totalRelativeWeight);
            for (int j = 0; j < discreteWeight; j++) {
                // optimization: direct access for fields "valueBase" and "numberOfParameters" of ParameterizedValue
                valueSamples_valueBase[i] = value.getValueBase();
                valueSamples_numberOfParameters[i] = (byte) value.getNumberOfParameters();
                i++;
            }
        }
    }
}
