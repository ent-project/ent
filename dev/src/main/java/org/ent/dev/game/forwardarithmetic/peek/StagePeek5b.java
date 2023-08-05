package org.ent.dev.game.forwardarithmetic.peek;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.dev.game.forwardarithmetic.ArithmeticForwardGame;
import org.ent.dev.game.forwardarithmetic.StageBase;
import org.ent.dev.randnet.PortalValue;
import org.ent.dev.randnet.RandomNetCreator;
import org.ent.dev.randnet.ValueDrawing;
import org.ent.dev.randnet.ValueDrawingHp;
import org.ent.dev.variation.ArrowMixMutation;
import org.ent.hyper.DoubleHyperDefinition;
import org.ent.hyper.HyperManager;
import org.ent.hyper.IntHyperDefinition;
import org.ent.hyper.RemoteHyperManager;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.net.util.NetCopy2;
import org.ent.net.util.RandomUtil;
import org.ent.webui.WebUI;
import org.ent.webui.WebUiStoryOutput;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

public class StagePeek5b extends StageBase<StagePeek5b.Solution> {

    private static final boolean WEB_UI = true;
    public static final boolean REPLAY_HITS = false || WEB_UI;
    private static final boolean ANNOTATIONS = true;

    public static final IntHyperDefinition HYPER_MAX_ATTEMPTS = new IntHyperDefinition("max-attempts", 1, 400);
    public static final IntHyperDefinition HYPER_MAX_STEPS = new IntHyperDefinition("max-steps", 3, 200);
    public static final DoubleHyperDefinition HYPER_FRAC_PORTALS = new DoubleHyperDefinition("fraction_portals", 0.0, 1.0);
    public static final IntHyperDefinition HYPER_NO_NODES_ADD_ON = new IntHyperDefinition("no-nodes-add-on", 0, 100);
    public static final DoubleHyperDefinition HYPER_ARROW_MIX_STRENGTH = new DoubleHyperDefinition("arrow-mix-strength", 0.0, 1.0);

    public static final String HYPER_GROUP_STAGE4 = "stage4";
    public static final String HYPER_GROUP_STAGE5B_DRAWING = "stage5b-drawing";

    private final int maxAttempts;
    private final int maxSteps;
    private final int numberOfNodesAddOn;
    private final double arrowMixStrength;

    private final UniformRandomProvider randNetSeeds;
    private final UniformRandomProvider randMixerSeeds;

    private final ValueDrawing drawing;

    private final StagePeek4 stagePeek4;

    public StagePeek5b(HyperManager hyperManager, UniformRandomProvider randMaster) {
        super(randMaster);

        this.drawing = new ValueDrawingPeek5b(hyperManager.group(HYPER_GROUP_STAGE5B_DRAWING));

        this.maxAttempts = hyperManager.get(HYPER_MAX_ATTEMPTS);
        this.maxSteps = hyperManager.get(HYPER_MAX_STEPS);
        this.numberOfNodesAddOn = hyperManager.get(HYPER_NO_NODES_ADD_ON);
        this.arrowMixStrength = hyperManager.get(HYPER_ARROW_MIX_STRENGTH);

        this.randNetSeeds = RandomUtil.newRandom2(randMaster.nextLong());
        this.randMixerSeeds = RandomUtil.newRandom2(randMaster.nextLong());

        this.stagePeek4 = new StagePeek4(
                hyperManager.group(HYPER_GROUP_STAGE4),
                RandomUtil.newRandom2(randMaster.nextLong()));
        this.stagePeek4.setFinalStep(true);
    }

    public static void main(String[] args) throws IOException {
        if (WEB_UI) {
            WebUI.setUpJavalin();
        }
        new StagePeek5b.Factory().main(1);
    }

    static class Factory extends StageFactory<StagePeek5b> {

        @Override
        public StagePeek5b createStage(RemoteHyperManager hyperManager, int indexTrial) {
            StagePeek5b dev = new StagePeek5b(hyperManager, RandomUtil.newRandom2(randomTrials.nextLong()));
//            dev.setTrialMaxEvaluations(100);
            dev.setTrialMaxDuration(Duration.ofSeconds(40));
            return dev;
        }

        @Override
        public void registerHyperparameters(HyperManager hyperCollector) {
            new StagePeek4.Factory().registerHyperparameters(hyperCollector.group(HYPER_GROUP_STAGE4));
            ValueDrawingPeek5b.registerHyperparameters(hyperCollector.group(HYPER_GROUP_STAGE5B_DRAWING));
            hyperCollector.get(HYPER_MAX_ATTEMPTS);
            hyperCollector.get(HYPER_MAX_STEPS);
            hyperCollector.get(HYPER_NO_NODES_ADD_ON);
            hyperCollector.get(HYPER_ARROW_MIX_STRENGTH);
        }

        @Override
        public void fixHyperparameters(HyperManager hyperManager) {
            new StagePeek4.Factory().fixHyperparameters(hyperManager.group(HYPER_GROUP_STAGE4));
            hyperManager.group(HYPER_GROUP_STAGE4).overrideLines("""
                fragment-context 2
                    """);
            hyperManager.group(HYPER_GROUP_STAGE5B_DRAWING).fixLines("""
                    fraction_commands 0.27390593155980114
                    fraction_major_commands 0.7750135486307677
                    fraction_major_split 0.8100149043972289
                    fraction_portals 0.1
                    fraction_set 0.4709540476189268
                    """);
            hyperManager.fix(HYPER_MAX_ATTEMPTS, 300);
            hyperManager.fix(HYPER_MAX_STEPS, 30);
            hyperManager.fix(HYPER_NO_NODES_ADD_ON, 40);
            hyperManager.fix(HYPER_ARROW_MIX_STRENGTH, 0.9);


        }
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

            boolean hit = operationListener.found != null;
            if (hit) {
                Solution solution = new Solution(upstreamPeek4, netAddOnSeed, mixerSeed, operationListener);
                submitSolution(solution);
                if (REPLAY_HITS) {
                    WebUiStoryOutput.addStoryWithAnnouncement("StagePeek3-%s-%s".formatted(indexTrial, indexEvaluation),
                            () -> stagePeek4.stagePeek3().replayWithDetails(solution.upstreamPeek4().upstreamPeek3()));
                    WebUiStoryOutput.addStoryWithAnnouncement("StagePeek4-%s-%s-unmixed".formatted(indexTrial, indexEvaluation),
                            () -> stagePeek4.replayUnmixed(solution.upstreamPeek4()));
                    WebUiStoryOutput.addStoryWithAnnouncement("StagePeek4-%s-%s".formatted(indexTrial, indexEvaluation),
                            () -> stagePeek4.replayWithDetails(solution.upstreamPeek4()));
                    WebUiStoryOutput.addStoryWithAnnouncement("StagePeek5b-%s-%s".formatted(indexTrial, indexEvaluation),
                            () -> replayWithDetails(solution));
                }
                numHit++;
                break;
            }
        }
    }

    private void replayWithDetails(Solution solution) {

        ArithmeticForwardGame game = setUpGame(solution.upstreamPeek4(), solution.netAddOnSeed(), solution.mixerSeed());
        game.setMaxSteps(solution.operationListener().found + 1);
        OperationExecutionEntListener operationListener = new OperationExecutionEntListener(game);
        game.getEnt().addEventListener(operationListener);

        game.setVerbose(true);

        game.execute();
    }

    @NotNull
    private ArithmeticForwardGame setUpGame(StagePeek4.Solution upstreamPeek4, long netAddOnSeed, long mixerSeed) {
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

        ArrowMixMutation mixMutation = new ArrowMixMutation(arrowMixStrength, net, RandomUtil.newRandom2(mixerSeed));
        mixMutation.setSourceRange(previousSize, net.getNodes().size());
        mixMutation.setDestinationRange(0, previousSize);
        mixMutation.execute();


        int realMaxSteps = upstreamPeek4.readOperandsListener().allFound + 1 + maxSteps;
        ArithmeticForwardGame game0 = upstreamPeek4.upstreamPeek3().upstreamPeek1().game();

        return new ArithmeticForwardGame(
                game0.getOperand1(),
                game0.getOperand2(),
                game0.getOperation(),
                net,
                realMaxSteps);
    }

    public record Solution(StagePeek4.Solution upstreamPeek4, long netAddOnSeed,
                           long mixerSeed, OperationExecutionEntListener operationListener) {
    }

    private class ValueDrawingPeek5b extends ValueDrawingHp {

        public static void registerHyperparameters(HyperManager hyperManager) {
            hyperManager.get(HYPER_FRAC_PORTALS);
            ValueDrawingHp.registerHyperparameter(hyperManager);
        }

        public ValueDrawingPeek5b(HyperManager hyperManager) {
            super(hyperManager);
        }

        @Override
        protected DistributionNode initializeDistribution() {
            double fracPortal = hyperManager.get(HYPER_FRAC_PORTALS);
            log.info("got HPs: fracPortal={}", fracPortal);
            return new DistributionSplit(fracPortal)
                    .first(new DistributionLeaf()
                            .add(new PortalValue(0, 1))
                            .add(new PortalValue(0, null))
                            .add(new PortalValue(null, 1)))
                    .rest(new DistributionLeaf().add(Operations.SET_VALUE_OPERATION));
        }
    }

}
