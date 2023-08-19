package org.ent.dev.game.forwardarithmetic.peek;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.NopNetEventListener;
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

import java.io.IOException;
import java.time.Duration;
import java.util.List;

public class StagePeek6 extends StageBase<StagePeek6.Solution> {

    private static final boolean WEB_UI = false;
    public static final boolean REPLAY_HITS = false || WEB_UI;
    private static final boolean ANNOTATIONS = false || WEB_UI;

    public static final IntHyperDefinition HYPER_MAX_ATTEMPTS = new IntHyperDefinition("max-attempts", 1, 20_000);
    public static final IntHyperDefinition HYPER_MAX_STEPS = new IntHyperDefinition("max-steps", 3, 100);
    public static final DoubleHyperDefinition HYPER_FRAC_PORTALS = new DoubleHyperDefinition("fraction_portals", 0.0, 1.0);
    public static final IntHyperDefinition HYPER_NO_NODES_ADD_ON = new IntHyperDefinition("no-nodes-add-on", 1, 40);
    public static final DoubleHyperDefinition HYPER_ARROW_MIX_STRENGTH_TARGET = new DoubleHyperDefinition("arrow-mix-strength-target", 0.0, 1.5);
    public static final DoubleHyperDefinition HYPER_ARROW_MIX_STRENGTH = new DoubleHyperDefinition("arrow-mix-strength", 0.0, 1.0);
    public static final DoubleHyperDefinition HYPER_ARROW_MIX_STRENGTH2 = new DoubleHyperDefinition("arrow-mix-strength2", 0.0, 1.0);

    public static final String HYPER_GROUP_STAGE5b = "stage5b";
    public static final String HYPER_GROUP_STAGE6_DRAWING = "stage6-drawing";

    private final int maxAttempts;
    private final int maxSteps;
    private final int numberOfNodesAddOn;
    private final double arrowMixStrengthTarget;
    private final double arrowMixStrength;
    private final double arrowMixStrength2;

    private final UniformRandomProvider randNetSeeds;
    private final UniformRandomProvider randMixerSeeds;

    private final ValueDrawing drawing;

    private final StagePeek5b stagePeek5b;

    public StagePeek6(HyperManager hyperManager, UniformRandomProvider randMaster) {
        super(randMaster);

        this.drawing = new ValueDrawingPeek6(hyperManager.group(HYPER_GROUP_STAGE6_DRAWING));

        this.maxAttempts = hyperManager.get(HYPER_MAX_ATTEMPTS);
        this.maxSteps = hyperManager.get(HYPER_MAX_STEPS);
        this.numberOfNodesAddOn = hyperManager.get(HYPER_NO_NODES_ADD_ON);
        this.arrowMixStrengthTarget = hyperManager.get(HYPER_ARROW_MIX_STRENGTH_TARGET);
        this.arrowMixStrength = hyperManager.get(HYPER_ARROW_MIX_STRENGTH);
        this.arrowMixStrength2 = hyperManager.get(HYPER_ARROW_MIX_STRENGTH2);

        this.randNetSeeds = RandomUtil.newRandom2(randMaster.nextLong());
        this.randMixerSeeds = RandomUtil.newRandom2(randMaster.nextLong());

        this.stagePeek5b = new StagePeek5b(
                hyperManager.group(HYPER_GROUP_STAGE5b),
                RandomUtil.newRandom2(randMaster.nextLong()));
    }

    public static void main(String[] args) throws IOException {
        if (WEB_UI) {
            WebUI.setUpJavalin();
        }
        new StagePeek6.Factory().main(5000);
    }

    static class Factory extends StageFactory<StagePeek6> {

        @Override
        public StagePeek6 createStage(RemoteHyperManager hyperManager, int indexTrial) {
            long masterSeed = randomTrials.nextLong();
            log.info("using master seed {} for trial {}", masterSeed, indexTrial);
            StagePeek6 dev = new StagePeek6(hyperManager, RandomUtil.newRandom2(masterSeed));
//            dev.setTrialMaxEvaluations(100);
            dev.setTrialMaxDuration(Duration.ofSeconds(25));
            return dev;
        }

        @Override
        public void registerHyperparameters(HyperManager hyperCollector) {
            new StagePeek5b.Factory().registerHyperparameters(hyperCollector.group(HYPER_GROUP_STAGE5b));
            ValueDrawingPeek6.registerHyperparameters(hyperCollector.group(HYPER_GROUP_STAGE6_DRAWING));
            hyperCollector.get(HYPER_MAX_ATTEMPTS);
            hyperCollector.get(HYPER_MAX_STEPS);
            hyperCollector.get(HYPER_NO_NODES_ADD_ON);
            hyperCollector.get(HYPER_ARROW_MIX_STRENGTH_TARGET);
            hyperCollector.get(HYPER_ARROW_MIX_STRENGTH);
            hyperCollector.get(HYPER_ARROW_MIX_STRENGTH2);
        }

        @Override
        public void fixHyperparameters(HyperManager hyperManager) {
            new StagePeek5b.Factory().fixHyperparameters(hyperManager.group(HYPER_GROUP_STAGE5b));
//            hyperManager.fix(HYPER_MAX_ATTEMPTS, 2300);
//            hyperManager.fix(HYPER_MAX_STEPS, 30);
//            hyperManager.fix(HYPER_NO_NODES_ADD_ON, 30);
//            hyperManager.fix(HYPER_ARROW_MIX_STRENGTH_TARGET, 0.9);
//            hyperManager.fix(HYPER_ARROW_MIX_STRENGTH, 0.2);
//            hyperManager.fix(HYPER_ARROW_MIX_STRENGTH2, 0.15);
//            hyperManager.group(HYPER_GROUP_STAGE6_DRAWING).fixLines("""
//                    fraction_commands 0.6127454263266574
//                    fraction_major_commands 0.03186006057262768
//                    fraction_major_split 0.511007022322576
//                    fraction_portals 0.42748436419696856
//                    fraction_set 0.5607673547553892
//                    """);
            hyperManager.fixLines("""
                    arrow-mix-strength 0.5060658485779603
                    arrow-mix-strength-target 0.9665539776024774
                    arrow-mix-strength2 0.05168681955092405
                    max-attempts 17211
                    max-steps 59
                    no-nodes-add-on 15
                    stage6-drawing.fraction_commands 0.007112443104366477
                    stage6-drawing.fraction_major_commands 0.4102176096087161
                    stage6-drawing.fraction_major_split 0.6991223933427323
                    stage6-drawing.fraction_portals 0.7187635553562264
                    stage6-drawing.fraction_set 0.6892544914493772                    
                        """);
        }
    }

    @Override
    protected void nextEvaluation() {
        StagePeek5b.Solution upstreamPeek5b = stagePeek5b.getNextSolution();
        Net net0 = stagePeek5b.setUpNet(upstreamPeek5b.upstreamPeek4(), upstreamPeek5b.netAddOnSeed(), upstreamPeek5b.mixerSeed());

        for (int indexAttempt = 0; indexAttempt < maxAttempts; indexAttempt++) {
            long netAddOnSeed = randNetSeeds.nextLong();
            long mixerSeed = randMixerSeeds.nextLong();
            ArithmeticForwardGame game = setUpGame(net0, upstreamPeek5b, netAddOnSeed, mixerSeed);
            RightAnswerListener rightAnswerListener = new RightAnswerListener(game);
            game.getAnswerNet().addEventListener(rightAnswerListener);

            game.execute();

            boolean hit = rightAnswerListener.foundStep != null;
            if (hit) {
                Solution solution = new Solution(net0, upstreamPeek5b, netAddOnSeed, mixerSeed, rightAnswerListener);
                submitSolution(solution);
                if (REPLAY_HITS) {
                    WebUiStoryOutput.addStoryWithAnnouncement("StagePeek5b-%s-%s".formatted(indexTrial, indexEvaluation),
                            () -> stagePeek5b.replayWithDetails(solution.upstreamPeek5b()));
                    WebUiStoryOutput.addStoryWithAnnouncement("StagePeek6-%s-%s-%s".formatted(indexTrial, indexEvaluation, indexAttempt),
                            () -> replayWithDetails(solution));
                }
                numHit++;
                break;
            }
        }
    }

    private void replayWithDetails(Solution solution) {
        ArithmeticForwardGame game = setUpGame(
                solution.net0(),
                solution.upstreamPeek5b(),
                solution.netAddOnSeed(),
                solution.mixerSeed());
        RightAnswerListener rightAnswerListener = new RightAnswerListener(game);
        game.getAnswerNet().addEventListener(rightAnswerListener);

        game.setVerbose(true);

        game.execute();
    }

    private ArithmeticForwardGame setUpGame(Net net0, StagePeek5b.Solution upstreamPeek5b, long netAddOnSeed, long mixerSeed) {
        Net net = setUpNet(net0, upstreamPeek5b, netAddOnSeed, mixerSeed);
        int realMaxSteps = upstreamPeek5b.operationListener().found + 1 + maxSteps;
        ArithmeticForwardGame game0 = upstreamPeek5b.upstreamPeek4().upstreamPeek3().upstreamPeek1().game();

        ArithmeticForwardGame game = new ArithmeticForwardGame(
                game0.getOperand1(),
                game0.getOperand2(),
                game0.getOperation(),
                net,
                realMaxSteps);
        game.initializeVerifier();
        return game;
    }

    private Net setUpNet(Net net0, StagePeek5b.Solution upstreamPeek5b, long netAddOnSeed, long mixerSeed) {
        Net net = NetCopy2.createCopy(net0);
        int previousSize = net.getNodes().size();

        RandomNetCreator netCreator = new RandomNetCreator(numberOfNodesAddOn, RandomUtil.newRandom2(netAddOnSeed), drawing);
        Net netAddOn = netCreator.drawNet();

        Node addOnRoot = netAddOn.getRoot();
        List<Node> netAddOnNodes = netAddOn.removeAllNodes();
        net.addNodes(netAddOnNodes);
        if (ANNOTATIONS) {
            for (Node netAddOnNode : netAddOnNodes) {
                net.appendAnnotation(netAddOnNode, "@6");
            }
        }
        Node stitchOriginAddOn = net.getNode(upstreamPeek5b.operationListener().rootWhenFound.getIndex());
        stitchOriginAddOn.setRightChild(addOnRoot, Purview.DIRECT);

        {
            Node target = upstreamPeek5b.operationListener().targetWhenFound;
            ArrowMixMutation mixMutationTarget = new ArrowMixMutation(arrowMixStrengthTarget, net, RandomUtil.newRandom2(mixerSeed + 9)) {
                @Override
                protected int resolveDestination(int index) {
                    return target.getIndex();
                }
            };
            mixMutationTarget.setSourceRange(previousSize, net.getNodes().size());
            mixMutationTarget.setDestinationRange(0, 1);
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

    public class Solution {
        private final Net net0;
        private final StagePeek5b.Solution upstreamPeek5b;
        private final long netAddOnSeed;
        private final long mixerSeed;
        private final RightAnswerListener rightAnswerListener;

        public Solution(Net net0, StagePeek5b.Solution upstreamPeek5b, long netAddOnSeed, long mixerSeed,
                        RightAnswerListener rightAnswerListener) {
            this.net0 = net0;
            this.upstreamPeek5b = upstreamPeek5b;
            this.netAddOnSeed = netAddOnSeed;
            this.mixerSeed = mixerSeed;
            this.rightAnswerListener = rightAnswerListener;
        }

        public Net net0() {
            return net0;
        }

        public StagePeek5b.Solution upstreamPeek5b() {
            return upstreamPeek5b;
        }

        public long netAddOnSeed() {
            return netAddOnSeed;
        }

        public long mixerSeed() {
            return mixerSeed;
        }

        public RightAnswerListener rightAnswerListener() {
            return rightAnswerListener;
        }

        public Net prepareNet() {
            return setUpNet(net0, upstreamPeek5b, netAddOnSeed, mixerSeed);
        }
    }

    public static class RightAnswerListener extends NopNetEventListener {

        private final ArithmeticForwardGame game;
        public Integer foundStep;
        public Node foundTarget;

        public RightAnswerListener(ArithmeticForwardGame game) {
            this.game = game;
        }

        @Override
        public void setValue(Node node, int previousValue, int newValue) {
            if (newValue == game.getExpectedSolution()) {
                foundStep = game.getStep();
                foundTarget = node;
                game.stopExecution();
            }
        }
    }

    private class ValueDrawingPeek6 extends ValueDrawingHp {

        public static void registerHyperparameters(HyperManager hyperManager) {
            hyperManager.get(HYPER_FRAC_PORTALS);
            ValueDrawingHp.registerHyperparameter(hyperManager);
        }

        public ValueDrawingPeek6(HyperManager hyperManager) {
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
