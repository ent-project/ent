package org.ent.dev.game.forwardarithmetic.peek;

import org.ent.dev.game.forwardarithmetic.readinfo.ValueDrawingWithPortals;
import org.ent.hyper.DoubleHyperDefinition;
import org.ent.hyper.HyperManager;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.net.node.cmd.veto.Conditions;

public class ValueDrawingWithPortalAndEvalFlow extends ValueDrawingWithPortals {
    public static DoubleHyperDefinition FRAC_EVAL_FLOW = new DoubleHyperDefinition("fraction_eval_flow", 0.0, 1.0);

    public ValueDrawingWithPortalAndEvalFlow(HyperManager hyperManager) {
        super(hyperManager);
    }

    public static void registerHyperparameters(HyperManager hyperManager) {
        ValueDrawingWithPortals.registerHyperparameter(hyperManager);
        hyperManager.get(FRAC_EVAL_FLOW);
    }

    @Override
    protected DistributionSplit defaultDistribution() {
        double fracCommands = hyperManager.get(FRAC_COMMANDS);
        double fracMajorCommands = hyperManager.get(FRAC_MAJOR_COMMANDS);
        double fracMajorSplit = hyperManager.get(FRAC_MAJOR_SPLIT);
        double fracSet = hyperManager.get(FRAC_SET);
        double fracEvalFlow = hyperManager.get(FRAC_EVAL_FLOW);
//            log.info("got HPs: fracCommands={}, fracMajorCommands={}, fracMajorSplit={}, fracSet={}, fracEvalFlow={}", fracCommands, fracMajorCommands, fracMajorSplit, fracSet, fracEvalFlow);

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
                                .rest(new DistributionSplit(fracEvalFlow)
                                        // eval_flow
                                        .first(new DistributionLeaf()
                                                .add(Operations.EVAL_FLOW_OPERATION, WEIGHT2)
                                        )
                                        // remaining major commands
                                        .rest(new DistributionLeaf()
                                                .add(Operations.ANCESTOR_EXCHANGE_OPERATION, WEIGHT1)
                                                .add(Operations.DUP_OPERATION, WEIGHT1)
                                                .add(Operations.EVAL_OPERATION, WEIGHT2)
                                        )))
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
}
