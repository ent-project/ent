package org.ent.dev.game.forwardarithmetic.peek;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.dev.game.forwardarithmetic.ArithmeticForwardGame;
import org.ent.dev.game.forwardarithmetic.StageBase;
import org.ent.dev.randnet.RandomNetCreator;
import org.ent.dev.randnet.ValueDrawing;
import org.ent.dev.variation.ArrowMixMutation;
import org.ent.hyper.DoubleHyperDefinition;
import org.ent.hyper.HyperManager;
import org.ent.hyper.IntHyperDefinition;
import org.ent.hyper.RemoteHyperManager;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.operation.TriOperation;
import org.ent.net.util.NetCopy2;
import org.ent.net.util.RandomUtil;
import org.ent.webui.WebUI;
import org.ent.webui.WebUiStoryOutput;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class StagePeek5b extends StageBase<StagePeek5b.Solution> {

    private static final boolean WEB_UI = false;
    public static final boolean REPLAY_HITS = false || WEB_UI;
    private static final boolean ANNOTATIONS = true || WEB_UI;

    public static final IntHyperDefinition HYPER_MAX_ATTEMPTS = new IntHyperDefinition("max-attempts", 1, 2000);
    public static final IntHyperDefinition HYPER_MAX_STEPS = new IntHyperDefinition("max-steps", 3, 200);
    public static final IntHyperDefinition HYPER_NO_NODES_ADD_ON = new IntHyperDefinition("no-nodes-add-on", 1, 100);
    public static final DoubleHyperDefinition HYPER_ARROW_MIX_STRENGTH_TARGET = new DoubleHyperDefinition("arrow-mix-strength-target", 0.0, 1.0);
    public static final DoubleHyperDefinition HYPER_ARROW_MIX_STRENGTH = new DoubleHyperDefinition("arrow-mix-strength", 0.0, 1.0);
    public static final DoubleHyperDefinition HYPER_ARROW_MIX_STRENGTH2 = new DoubleHyperDefinition("arrow-mix-strength2", 0.0, 1.0);

    public static final String HYPER_GROUP_STAGE4 = "stage4";
    public static final String HYPER_GROUP_STAGE5B_DRAWING = "stage5b-drawing";

    private final int maxAttempts;
    private final int maxSteps;
    private final int numberOfNodesAddOn;
    private final double arrowMixStrengthTarget;
    private final double arrowMixStrength;
    private final double arrowMixStrength2;

    private final UniformRandomProvider randNetSeeds;
    private final UniformRandomProvider randMixerSeeds;
    private final UniformRandomProvider randTargets;

    private final ValueDrawing drawing;

    private final StagePeek4 stagePeek4;

    private int numDoubleCheckFailed;

    public StagePeek5b(HyperManager hyperManager, UniformRandomProvider randMaster) {
        super(randMaster);

        this.drawing = new ValueDrawingWithPortalAndEvalFlow(hyperManager.group(HYPER_GROUP_STAGE5B_DRAWING));

        this.maxAttempts = hyperManager.get(HYPER_MAX_ATTEMPTS);
        this.maxSteps = hyperManager.get(HYPER_MAX_STEPS);
        this.numberOfNodesAddOn = hyperManager.get(HYPER_NO_NODES_ADD_ON);
        this.arrowMixStrengthTarget = hyperManager.get(HYPER_ARROW_MIX_STRENGTH_TARGET);
        this.arrowMixStrength = hyperManager.get(HYPER_ARROW_MIX_STRENGTH);
        this.arrowMixStrength2 = hyperManager.get(HYPER_ARROW_MIX_STRENGTH2);

        this.randNetSeeds = RandomUtil.newRandom2(randMaster.nextLong());
        this.randMixerSeeds = RandomUtil.newRandom2(randMaster.nextLong());
        this.randTargets = RandomUtil.newRandom2(randMaster.nextLong());

        this.stagePeek4 = new StagePeek4(
                hyperManager.group(HYPER_GROUP_STAGE4),
                RandomUtil.newRandom2(randMaster.nextLong()));
        this.stagePeek4.setFinalStep(true);
    }

    public static void main(String[] args) throws IOException {
        if (WEB_UI) {
            WebUI.setUpJavalin();
        }
        new StagePeek5b.Factory().main(10);
    }

    static class Factory extends StageFactory<StagePeek5b> {

        @Override
        protected String getStudyName() {
            return super.getStudyName() + "_no_evalflow2";
        }

        @Override
        public StagePeek5b createStage(RemoteHyperManager hyperManager, int indexTrial) {
            long masterSeed = randomTrials.nextLong();
            log.info("using master seed {} for trial {}", masterSeed, indexTrial);
            StagePeek5b dev = new StagePeek5b(hyperManager, RandomUtil.newRandom2(masterSeed));
//            dev.setTrialMaxEvaluations(5);
            dev.setTrialMaxDuration(Duration.ofSeconds(12));
            return dev;
        }

        @Override
        public void registerHyperparameters(HyperManager hyperCollector) {
            new StagePeek4.Factory().registerHyperparameters(hyperCollector.group(HYPER_GROUP_STAGE4));
            ValueDrawingWithPortalAndEvalFlow.registerHyperparameters(hyperCollector.group(HYPER_GROUP_STAGE5B_DRAWING));
            hyperCollector.get(HYPER_MAX_ATTEMPTS);
            hyperCollector.get(HYPER_MAX_STEPS);
            hyperCollector.get(HYPER_NO_NODES_ADD_ON);
            hyperCollector.get(HYPER_ARROW_MIX_STRENGTH_TARGET);
            hyperCollector.get(HYPER_ARROW_MIX_STRENGTH);
            hyperCollector.get(HYPER_ARROW_MIX_STRENGTH2);
        }

        @Override
        public void fixHyperparameters(HyperManager hyperManager) {
            new StagePeek4.Factory().fixHyperparameters(hyperManager.group(HYPER_GROUP_STAGE4));
            hyperManager.group(HYPER_GROUP_STAGE4).overrideLines("""
                    fragment-context 2
                        """);
//            hyperManager.group(HYPER_GROUP_STAGE5B_DRAWING).fixLines("""
//                    fraction_commands 0.27390593155980114
//                    fraction_major_commands 0.7750135486307677
//                    fraction_major_split 0.8100149043972289
//                    fraction_portals 0.3
//                    fraction_set 0.4709540476189268
//                    """);
//            hyperManager.fix(HYPER_MAX_ATTEMPTS, 300);
//            hyperManager.fix(HYPER_MAX_STEPS, 30);
//            hyperManager.fix(HYPER_NO_NODES_ADD_ON, 40);
//            hyperManager.fix(HYPER_ARROW_MIX_STRENGTH, 0.9);

            hyperManager.group(HYPER_GROUP_STAGE4).clear(StagePeek4.HYPER_FRAGMENT_CONTEXT);
            hyperManager.group(HYPER_GROUP_STAGE4).clear(StagePeek4.HYPER_MAX_ATTEMPTS);
            hyperManager.group(HYPER_GROUP_STAGE4).clear(StagePeek4.HYPER_ARROW_MIX_STRENGTH);

            hyperManager.fixLines("""
                                        arrow-mix-strength 0.5895591246727114
                                        max-attempts 385
                    //                    max-steps 12
                    //                    no-nodes-add-on 28
                                        stage4.arrow-mix-strength 0.9243883842949241
                                        stage4.fragment-context 1
                                        stage4.max-attempts 55
                    //                    stage5b-drawing.fraction_commands 0.6127454263266574
                    //                    stage5b-drawing.fraction_major_commands 0.03186006057262768
                    //                    stage5b-drawing.fraction_major_split 0.511007022322576
                    //                    stage5b-drawing.fraction_portals 0.42748436419696856
                    //                    stage5b-drawing.fraction_set 0.5607673547553892
                                        """);
//            hyperManager.fix(HYPER_ARROW_MIX_STRENGTH2, 0.1);
            hyperManager.clear(HYPER_ARROW_MIX_STRENGTH);
            hyperManager.clear(HYPER_MAX_ATTEMPTS);
//            hyperManager.fix(HYPER_ARROW_MIX_STRENGTH_TARGET, 0.9);
////            hyperManager.fix(HYPER_ARROW_MIX_STRENGTH, 0.3);
//            hyperManager.fix(HYPER_ARROW_MIX_STRENGTH, 0.0);
////            hyperManager.fix(HYPER_ARROW_MIX_STRENGTH2, 0.15);
//            hyperManager.fix(HYPER_ARROW_MIX_STRENGTH2, 0.0);
//            hyperManager.fix(HYPER_MAX_ATTEMPTS, 230);
            hyperManager.fix(HYPER_MAX_ATTEMPTS, 20_000);

            hyperManager.group(HYPER_GROUP_STAGE4).override(StagePeek4.HYPER_LOOSE_STITCHING, 1);

            hyperManager.fixLines("""
                                                arrow-mix-strength 0.15502819836639187
                                                arrow-mix-strength-target 0.9576915738511704
                                                arrow-mix-strength2 0.20920002955720085
                                                max-steps 49
                                                no-nodes-add-on 13
                    //                            stage5b-drawing.fraction_commands 0.320766197908282
                    //                            stage5b-drawing.fraction_major_commands 0.8308019880776694
                    //                            stage5b-drawing.fraction_major_split 0.6954265118753846
                    //                            stage5b-drawing.fraction_portals 0.12007412696589982
                    //                            stage5b-drawing.fraction_set 0.5736090916799118
                                        """);
            hyperManager.group(HYPER_GROUP_STAGE5B_DRAWING).fix(ValueDrawingWithPortalAndEvalFlow.FRAC_EVAL_FLOW, 0.0);


            hyperManager.fixLines("""
                    stage5b-drawing.fraction_commands 0.5820685541375051
                    stage5b-drawing.fraction_major_commands 0.42328739486591704
                    stage5b-drawing.fraction_major_split 0.15312902067600329
                    stage5b-drawing.fraction_portals 0.25005891352994464
                    stage5b-drawing.fraction_set 0.08918501613305074
                    """);
        }
    }

    @Override
    protected void printRunInfo(Duration duration) {
        log.info("potential hit, bit double check failed: {}", numDoubleCheckFailed);
        super.printRunInfo(duration);
    }

    @Override
    protected void nextEvaluation() {
        StagePeek4.Solution upstreamPeek4 = stagePeek4.getNextSolution();
        upstreamPeek4.applyMix();

        for (int indexAttempt = 0; indexAttempt < maxAttempts; indexAttempt++) {
            long netAddOnSeed = randNetSeeds.nextLong();
            long mixerSeed = randMixerSeeds.nextLong();
            ArithmeticForwardGame game = setUpGame(upstreamPeek4, netAddOnSeed, mixerSeed);

            OperationExecutionEntListener operationListener = new OperationExecutionEntListener(game);
            game.getEnt().addEventListener(operationListener);

            game.execute();

            boolean hit = false;
            boolean maybeHit = operationListener.found != null;
            Solution solution = null;
            if (maybeHit) {
                solution = new Solution(upstreamPeek4, netAddOnSeed, mixerSeed, operationListener);
                if (verify(solution)) {
                    hit = true;
                } else {
                    numDoubleCheckFailed++;
                }
            }
            if (hit) {
                submitSolution(solution);
                if (REPLAY_HITS) {
                    Solution solution_final = solution;
                    WebUiStoryOutput.addStoryWithAnnouncement("StagePeek3-%s-%s".formatted(indexTrial, indexEvaluation),
                            () -> stagePeek4.stagePeek3().replayWithDetails(solution_final.upstreamPeek4().upstreamPeek3()));
                    WebUiStoryOutput.addStoryWithAnnouncement("StagePeek4-%s-%s-unmixed".formatted(indexTrial, indexEvaluation),
                            () -> stagePeek4.replayUnmixed(solution_final.upstreamPeek4()));
                    WebUiStoryOutput.addStoryWithAnnouncement("StagePeek4-%s-%s".formatted(indexTrial, indexEvaluation),
                            () -> stagePeek4.replayWithDetails(solution_final.upstreamPeek4()));
                    WebUiStoryOutput.addStoryWithAnnouncement("StagePeek5b-%s-%s-%s".formatted(indexTrial, indexEvaluation, indexAttempt),
                            () -> replayWithDetails(solution_final));
                }
                numHit++;
                break;
            }
        }
    }

    private boolean verify(Solution solution) {
        for (int i = 0; i < 10; i++) {
            if (!verifySingle(solution)) {
                return false;
            }
        }
        return true;
    }

    private boolean verifySingle(Solution solution) {
        int operand1 = ArithmeticForwardGame.drawOperand(randTargets);
        int operand2 = ArithmeticForwardGame.drawOperand(randTargets);
        ArithmeticForwardGame game0 = solution.upstreamPeek4().upstreamPeek3().upstreamPeek1().game();
        TriOperation operation;
        do {
            operation = ArithmeticForwardGame.drawOperation(randTargets);
        } while (operation == game0.getOperation());

        ArithmeticForwardGame game = setUpGame(
                solution.upstreamPeek4(), solution.netAddOnSeed(), solution.mixerSeed(),
                operand1, operand2, operation);

        OperationExecutionEntListener operationListener = new OperationExecutionEntListener(game);
        game.getEnt().addEventListener(operationListener);

        game.execute();
        return operationListener.found != null;
    }

    public void replayWithDetails(Solution solution) {
        ArithmeticForwardGame game = setUpGame(solution.upstreamPeek4(), solution.netAddOnSeed(), solution.mixerSeed());
        game.setMaxSteps(solution.operationListener().found + 1);
        OperationExecutionEntListener operationListener = new OperationExecutionEntListener(game);
        game.getEnt().addEventListener(operationListener);

        game.setVerbose(true);

        game.execute();
    }

    public ArithmeticForwardGame setUpGame(StagePeek4.Solution upstreamPeek4, long netAddOnSeed, long mixerSeed) {
        ArithmeticForwardGame game0 = upstreamPeek4.upstreamPeek3().upstreamPeek1().game();
        return setUpGame(upstreamPeek4, netAddOnSeed, mixerSeed,
                game0.getOperand1(), game0.getOperand2(), game0.getOperation());
    }

    public ArithmeticForwardGame setUpGame(StagePeek4.Solution upstreamPeek4, long netAddOnSeed, long mixerSeed, int operand1, int operand2, TriOperation operation) {
        Net net = setUpNet(upstreamPeek4, netAddOnSeed, mixerSeed);

        int realMaxSteps = upstreamPeek4.readOperandsListener().allFoundStep + 1 + maxSteps;
        return new ArithmeticForwardGame(
                operand1,
                operand2,
                operation,
                net,
                realMaxSteps);
    }

    public Net setUpNet(StagePeek4.Solution upstreamPeek4, long netAddOnSeed, long mixerSeed) {
        Net net = NetCopy2.createCopy(upstreamPeek4.net());
        int previousSize = net.getNodes().size();

        RandomNetCreator netCreator = new RandomNetCreator(numberOfNodesAddOn, RandomUtil.newRandom2(netAddOnSeed), drawing);
        Net netAddOn = netCreator.drawNet();

        Node addOnRoot = netAddOn.getRoot();
        List<Node> netAddOnNodes = netAddOn.removeAllNodes();
        net.addNodes(netAddOnNodes);
        if (ANNOTATIONS) {
            for (Node netAddOnNode : netAddOnNodes) {
                net.appendAnnotation(netAddOnNode, "@");
            }
        }
        Node stitchOriginAddOn = net.getNode(upstreamPeek4.nextStitchOrigin().getIndex());
//        Node rightChild = stitchOriginAddOn.getRightChild(Purview.DIRECT);
        stitchOriginAddOn.setRightChild(addOnRoot, Purview.DIRECT);
//        addOnRoot.setLeftChild(rightChild, Purview.DIRECT);

        {
            ArrayList<Integer> targets = new ArrayList<>(3);
            StagePeek4.Peek4ReadOperandsEntListener roListener = upstreamPeek4.readOperandsListener();
            for (ArithmeticForwardGame.OpTarget target : ArithmeticForwardGame.OpTarget.values()) {
                Node node = roListener.allFoundTargets.get(target);
                if (node.getNet().isCoreNet()) {
                    int index = node.getIndex();
                    if (index < net.getNodes().size()) {
                        targets.add(index);
                    }
                }
            }
            ArrowMixMutation mixMutationTarget = new ArrowMixMutation(arrowMixStrengthTarget, net, RandomUtil.newRandom2(mixerSeed + 9)) {
                @Override
                protected int resolveDestination(int index) {
                    return targets.get(index);
                }
            };
            mixMutationTarget.setSourceRange(previousSize, net.getNodes().size());
            mixMutationTarget.setDestinationRange(0, targets.size());
            mixMutationTarget.execute();
        }
        {
            ArrowMixMutation mixMutation = new ArrowMixMutation(arrowMixStrength, net, RandomUtil.newRandom2(mixerSeed));
            mixMutation.setSourceRange(previousSize, net.getNodes().size());
            mixMutation.setDestinationRange(0, previousSize);
            mixMutation.execute();
        }
        {
            ArrowMixMutation mixMutation2 = new ArrowMixMutation(arrowMixStrength2, net, RandomUtil.newRandom2(mixerSeed + 5));
            mixMutation2.execute();
        }
        return net;
    }

    public record Solution(StagePeek4.Solution upstreamPeek4, long netAddOnSeed,
                           long mixerSeed, OperationExecutionEntListener operationListener) {
    }

}
