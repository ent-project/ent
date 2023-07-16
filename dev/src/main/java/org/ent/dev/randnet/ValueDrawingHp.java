package org.ent.dev.randnet;

import org.ent.hyper.HyperManager;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.net.node.cmd.veto.Conditions;

public class ValueDrawingHp extends DefaultValueDrawing2 {

    private final HyperManager hyperManager;

    public ValueDrawingHp(HyperManager hyperManager) {
        this.hyperManager = hyperManager;
        ValueCollector collector = new ValueCollector();
        initializeValues(collector);
        if (hyperManager.isCollecting()) {
            return;
        }
        collector.buildValueSamples(50_000);
        valueSamples_valueBase = collector.getValueBase();
        valueSamples_numberOfParameters = collector.getNumberOfParameters();
    }

    @Override
    protected void initializeValues(DefaultValueDrawing2.ValueCollector collector) {
        float fracCommands = hyperManager.getFloat("fraction_commands", 0.0f, 1.0f);
        float fracMajorCommands = hyperManager.getFloat("fraction_major_commands", 0.0f, 1.0f);
        float fracMajorSplit = hyperManager.getFloat("fraction_major_split", 0.0f, 1.0f);
        float fracSet = hyperManager.getFloat("fraction_set", 0.0f, 1.0f);

        if (hyperManager.isCollecting()) {
            return;
        }

        float weightSet = fracCommands * fracMajorCommands * fracMajorSplit * fracSet;
        float weightSetValue = fracCommands * fracMajorCommands * fracMajorSplit * (1 - fracSet);
        float weightMajorOtherBase = fracCommands * fracMajorCommands * (1 - fracMajorSplit);
        float weightOtherBase = fracCommands * (1 - fracMajorCommands);
        float weightConditionBase = 1 - fracCommands;

        collector.addValueBase(Operations.SET_OPERATION, weightSet);
        collector.addValueBase(Operations.SET_VALUE_OPERATION, weightSetValue);

        collector.addValueBase(Commands.NOP, WEIGHT3 * weightMajorOtherBase);
        collector.addValueBase(Operations.ANCESTOR_EXCHANGE_OPERATION, WEIGHT1 * weightMajorOtherBase);
        collector.addValueBase(Operations.DUP_OPERATION, WEIGHT1 * weightMajorOtherBase);
        collector.addValueBase(Operations.EVAL_OPERATION, WEIGHT2 * weightMajorOtherBase);
        collector.addValueBase(Operations.EVAL_FLOW_OPERATION, WEIGHT2 * weightMajorOtherBase);

        collector.addValueBase(Operations.NEG_OPERATION, weightOtherBase * WEIGHT3);
        collector.addValueBase(Operations.INC_OPERATION, weightOtherBase * WEIGHT2);
        collector.addValueBase(Operations.DEC_OPERATION, weightOtherBase * WEIGHT2);
        collector.addValueBase(Operations.PLUS_OPERATION, weightOtherBase * WEIGHT2);
        collector.addValueBase(Operations.MINUS_OPERATION, weightOtherBase * WEIGHT3);
        collector.addValueBase(Operations.MULTIPLY_OPERATION, weightOtherBase * WEIGHT2);
        collector.addValueBase(Operations.MODULO_OPERATION, weightOtherBase * WEIGHT2);
        collector.addValueBase(Operations.XOR_OPERATION, weightOtherBase * WEIGHT2);
        collector.addValueBase(Operations.BITWISE_AND_OPERATION, weightOtherBase * WEIGHT2);
        collector.addValueBase(Operations.BITWISE_OR_OPERATION, weightOtherBase * WEIGHT2);
        collector.addValueBase(Operations.ROTATE_RIGHT_OPERATION, weightOtherBase * WEIGHT3);
        collector.addValueBase(Operations.SHIFT_LEFT_OPERATION, weightOtherBase * WEIGHT3);
        collector.addValueBase(Operations.SHIFT_RIGHT_OPERATION, weightOtherBase * WEIGHT3);

        collector.addValueBase(Conditions.IDENTICAL_CONDITION, false, weightConditionBase * WEIGHT3);
        collector.addValueBase(Conditions.IDENTICAL_CONDITION, true, weightConditionBase * WEIGHT3);
        collector.addValueBase(Conditions.SAME_VALUE_CONDITION, false, weightConditionBase * WEIGHT3);
        collector.addValueBase(Conditions.SAME_VALUE_CONDITION, true, weightConditionBase * WEIGHT3);
        collector.addValueBase(Conditions.GREATER_THAN_CONDITION, false, weightConditionBase * WEIGHT3);
        collector.addValueBase(Conditions.GREATER_THAN_CONDITION, true, weightConditionBase * WEIGHT3);
    }
}
