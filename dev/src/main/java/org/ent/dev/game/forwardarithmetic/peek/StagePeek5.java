package org.ent.dev.game.forwardarithmetic.peek;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.NopNetEventListener;
import org.ent.dev.game.forwardarithmetic.ArithmeticForwardGame;
import org.ent.dev.game.forwardarithmetic.StageBase;
import org.ent.dev.randnet.PortalValue;
import org.ent.dev.randnet.RandomNetCreator;
import org.ent.dev.randnet.ValueDrawing;
import org.ent.dev.randnet.ValueDrawingHp;
import org.ent.dev.trim2.TrimmingHelper;
import org.ent.dev.trim2.TrimmingListener;
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

public class StagePeek5 extends StageBase<StagePeek5.Solution> {

    private static final boolean WEB_UI = false;
    public static final boolean REPLAY_HITS = false || WEB_UI;

    public static final IntHyperDefinition HYPER_MAX_ATTEMPTS = new IntHyperDefinition("max-attempts", 1, 400);
    public static final DoubleHyperDefinition HYPER_ARROW_MIX_STRENGTH = new DoubleHyperDefinition("arrow-mix-strength", 0.0, 1.0);
    public static final IntHyperDefinition HYPER_MAX_STEPS = new IntHyperDefinition("max-steps", 3, 200);
    public static final DoubleHyperDefinition HYPER_FRAC_PORTALS = new DoubleHyperDefinition("fraction_portals", 0.0, 1.0);
    public static final IntHyperDefinition HYPER_NO_NODES_ADD_ON = new IntHyperDefinition("no-nodes-add-on", 0, 100);

    public static final String HYPER_GROUP_STAGE4 = "stage4";
    public static final String HYPER_GROUP_STAGE5 = "stage5";

    private final int maxAttempts;
    private final double arrowMixStrength;
    private final int maxSteps;
    private final int numberOfNodesAddOn;

    private final UniformRandomProvider randNetSeeds;

    private final ValueDrawing drawing;

    private final StagePeek4 stagePeek4;

    private final UniformRandomProvider randMixerSeeds;

    public StagePeek5(HyperManager hyperManager, UniformRandomProvider randMaster) {
        super(randMaster);

        this.drawing = new ValueDrawingPeek5(hyperManager.group(HYPER_GROUP_STAGE5));

        this.maxAttempts = hyperManager.get(HYPER_MAX_ATTEMPTS);
        this.arrowMixStrength = hyperManager.get(HYPER_ARROW_MIX_STRENGTH);
        this.maxSteps = hyperManager.get(HYPER_MAX_STEPS);
        this.numberOfNodesAddOn = hyperManager.get(HYPER_NO_NODES_ADD_ON);

        this.randMixerSeeds = RandomUtil.newRandom2(randMaster.nextLong());
        this.randNetSeeds = RandomUtil.newRandom2(randMaster.nextLong());

        this.stagePeek4 = new StagePeek4(
                hyperManager.group(HYPER_GROUP_STAGE4),
                RandomUtil.newRandom2(randMaster.nextLong()));
    }

    public static void main(String[] args) throws IOException {
        if (WEB_UI) {
            WebUI.setUpJavalin();
        }
        new StagePeek5.Factory().main(1);
    }

    static class Factory extends StageFactory<StagePeek5> {

        @Override
        public StagePeek5 createStage(RemoteHyperManager hyperManager, int indexTrial) {
            long masterSeed = randomTrials.nextLong();
//            masterSeed = -6220383428633220558L;
            log.info("using master seed {} for trial {}", masterSeed, indexTrial);
            StagePeek5 dev = new StagePeek5(hyperManager, RandomUtil.newRandom2(masterSeed));
//            dev.setTrialMaxEvaluations(2000);
            dev.setTrialMaxDuration(Duration.ofSeconds(40));
            return dev;
        }

        @Override
        public void registerHyperparameters(HyperManager hyperCollector) {
            new StagePeek4.Factory().registerHyperparameters(hyperCollector.group(HYPER_GROUP_STAGE4));
            ValueDrawingPeek5.registerHyperparameters(hyperCollector.group(HYPER_GROUP_STAGE5));
            hyperCollector.get(HYPER_MAX_ATTEMPTS);
            hyperCollector.get(HYPER_MAX_STEPS);
            hyperCollector.get(HYPER_ARROW_MIX_STRENGTH);
            hyperCollector.get(HYPER_NO_NODES_ADD_ON);
        }

        @Override
        public void fixHyperparameters(HyperManager hyperManager) {
            new StagePeek4.Factory().fixHyperparameters(hyperManager.group(HYPER_GROUP_STAGE4));
//            hyperManager.group(HYPER_GROUP_STAGE5).fixJson("""
//              {
//              'fraction_commands': 0.8867647226720414,
//              'fraction_major_commands': 0.9989939340398684,
//              'fraction_major_split': 0.924552028785329,
//              'fraction_portals': 0.6575066779888346,
//              'fraction_set': 0.9901816298961561,
//              }""");
//            hyperManager.fix(HYPER_MAX_ATTEMPTS, 400);
//            hyperManager.fix(HYPER_MAX_STEPS, 30);
//            hyperManager.fix(HYPER_ARROW_MIX_STRENGTH, 0.4);
//            hyperManager.fix(HYPER_NO_NODES_ADD_ON, 15);


//            hyperManager.fixLines("""
//                    arrow-mix-strength 0.11356510399013958
//                    max-attempts 130
//                    max-steps 44
//                    no-nodes-add-on 32
//                    stage5.fraction_commands 0.27390593155980114
//                    stage5.fraction_major_commands 0.7750135486307677
//                    stage5.fraction_major_split 0.8100149043972289
//                    stage5.fraction_portals 0.5644728778122936
//                    stage5.fraction_set 0.4709540476189268
//                    """);
//            hyperManager.fixJson("""
//                    {
//                        "arrow-mix-strength": 0.1308022805601076,
//                        "max-attempts": 76,
//                        "max-steps": 27,
//                        "no-nodes-add-on": 38,
//                        "stage5.fraction_commands": 0.3465275881318059,
//                        "stage5.fraction_major_commands": 0.6623882959056865,
//                        "stage5.fraction_major_split": 0.8116991409113717,
//                        "stage5.fraction_portals": 0.5413408564281402,
//                        "stage5.fraction_set": 0.3827882092985384
//                      }
//
//                    """);
            hyperManager.fixLines("""
                arrow-mix-strength 0.2657429460025833
                max-attempts 374
                max-steps 51
                no-nodes-add-on 0
                stage5.fraction_commands 0.45450798283473615
                stage5.fraction_major_commands 0.9429998098553173
                stage5.fraction_major_split 0.6978761172041702
                stage5.fraction_portals 0.9741148469006533
                stage5.fraction_set 0.4577902518296427                    
                    """);
            hyperManager.overrideLines("""
                stage4.arrow-mix-strength 0.9675791178324734
                stage4.fragment-context 2
                stage4.max-attempts 228
                stage4.max-steps 182
                    """);
        }
    }

    @Override
    protected void printRunInfo(Duration duration) {
        log.info("For trial {}", this.indexTrial);
        super.printRunInfo(duration);
    }

    @Override
    protected void nextEvaluation() {
        StagePeek4.Solution upstreamPeek4 = stagePeek4.getNextSolution();
        upstreamPeek4.applyMix();

        for (int indexAttempt = 0; indexAttempt < maxAttempts; indexAttempt++) {
            boolean interesting = false;// indexEvaluation == 2630 && indexAttempt ==54;

            long netAddOnSeed = randNetSeeds.nextLong();
            long mixerSeed = randMixerSeeds.nextLong();

            SetupResult setup = setUpGame(upstreamPeek4, netAddOnSeed, mixerSeed);

            if (interesting) {
                setup.game().setVerbose(true);
            }
            setup.game().execute();

//            boolean hit = setup.checkTargetValueListener().found != null;
            boolean hit = setup.operationExecutionEntListener().found != null;
            if (hit) {
//                if (setup.peek5EntListener().found2 != null) {
                    log.info("#{}-{} - hit!", indexEvaluation, indexAttempt);
//                } else {
//                    log.info("#{}-{} - HIT!", indexEvaluation, indexAttempt);
//                }
                // FIXME: verify with different op-values
                Solution solution = new Solution(
                        upstreamPeek4,
                        netAddOnSeed,
                        mixerSeed,
                        setup.checkTargetValueListener());
                submitSolution(solution);
                if (REPLAY_HITS) {
                    WebUiStoryOutput.addStoryWithAnnouncement("StagePeek5-%s-%s".formatted(indexTrial, indexEvaluation),
                            () -> replayWithDetails(solution));
                }
                numHit++;
                break;
            }
        }
    }

    private record SetupResult(ArithmeticForwardGame game, CheckTargetValueListener checkTargetValueListener, OperationExecutionEntListener operationExecutionEntListener) {
    }

    @NotNull
    private SetupResult setUpGame(StagePeek4.Solution upstreamPeek4, long netAddOnSeed, long mixerSeed) {
        Net netUpstream = upstreamPeek4.net();
        ArithmeticForwardGame game0 = upstreamPeek4.upstreamPeek3().upstreamPeek1().game();

        Net netAttempt = NetCopy2.createCopy(netUpstream);
        buildAndAttachAddOn(upstreamPeek4, netAttempt, netAddOnSeed);

        ArrowMixMutation mixMutation = new ArrowMixMutation(arrowMixStrength, netAttempt, RandomUtil.newRandom2(mixerSeed));
        mixMutation.execute();

        ArithmeticForwardGame game = new ArithmeticForwardGame(
                game0.getOperand1(),
                game0.getOperand2(),
                game0.getOperation(),
                netAttempt,
                maxSteps);
        CheckTargetValueListener checkTargetValueListener = new CheckTargetValueListener(game);
        game.initializeVerifier();
        game.getVerifierNet().addEventListener(checkTargetValueListener);
        game.getAnswerNet().addEventListener(checkTargetValueListener);
        OperationExecutionEntListener operationExecutionEntListener = new OperationExecutionEntListener(game);
        game.getEnt().addEventListener(operationExecutionEntListener);
        SetupResult result = new SetupResult(game, checkTargetValueListener, operationExecutionEntListener);
        return result;
    }

    private void buildAndAttachAddOn(StagePeek4.Solution upstreamPeek4, Net net, long netAddOnSeed) {
        if (numberOfNodesAddOn == 0) {
            return;
        }
        RandomNetCreator netCreator = new RandomNetCreator(numberOfNodesAddOn, RandomUtil.newRandom2(netAddOnSeed), drawing);
        Net netAddOn = netCreator.drawNet();
        Node addOnRoot = netAddOn.getRoot();
        List<Node> netAddOnNodes = netAddOn.removeAllNodes();
        net.addNodes(netAddOnNodes);
        Node stitchOriginAddOn = net.getNode(upstreamPeek4.nextStitchOrigin().getIndex());
        stitchOriginAddOn.setRightChild(addOnRoot, Purview.DIRECT);
    }

    private void replayWithDetails(Solution solution) {

        TrimmingListener trimmingListener;
        {
            SetupResult setup = setUpGame(solution.upstreamPeek4(), solution.netAddOnSeed(), solution.mixerSeed());

//            Net net = NetCopy2.createCopy(solution.upstreamPeek4().net());
//            buildAndAttachAddOn(solution.upstreamPeek4(), net, solution.netAddOnSeed());
//            ArrowMixMutation mixMutation = new ArrowMixMutation(arrowMixStrength, net, RandomUtil.newRandom2(solution.mixerSeed()));
//            mixMutation.execute();
//            ArithmeticForwardGame game = setUpGame(game0, net);
            Net net = setup.game().getEnt().getNet();
            trimmingListener = new TrimmingListener(net.getNodes().size());
            net.addEventListener(trimmingListener);

            setup.game().execute();
        }

        SetupResult setup = setUpGame(solution.upstreamPeek4(), solution.netAddOnSeed(), solution.mixerSeed());
        Net net = setup.game().getEnt().getNet();
//        Net net = NetCopy2.createCopy(solution.upstreamPeek4().net());
//        buildAndAttachAddOn(solution.upstreamPeek4(), net, solution.netAddOnSeed());
//        ArrowMixMutation mixMutation = new ArrowMixMutation(arrowMixStrength, net, RandomUtil.newRandom2(solution.mixerSeed()));
//        mixMutation.execute();
        TrimmingHelper.trim(net, trimmingListener);

//        ArithmeticForwardGame game = setUpGame(game0, net);
        setup.game().setVerbose(true);

        setup.game().execute();
        log.info("replay done.");
    }

    private ArithmeticForwardGame setUpGame(ArithmeticForwardGame game0, Net net) {
        ArithmeticForwardGame game = new ArithmeticForwardGame(
                game0.getOperand1(),
                game0.getOperand2(),
                game0.getOperation(),
                net,
                maxSteps);
        CheckTargetValueListener checkTargetValueListener = new CheckTargetValueListener(game);
        game.initializeVerifier();
        game.getVerifierNet().addEventListener(checkTargetValueListener);
        game.getAnswerNet().addEventListener(checkTargetValueListener);
        OperationExecutionEntListener operationExecutionEntListener = new OperationExecutionEntListener(game);
        game.getEnt().addEventListener(operationExecutionEntListener);
        return game;
    }

    private class CheckTargetValueListener extends NopNetEventListener {
        protected final ArithmeticForwardGame game;
        Integer found;

        public CheckTargetValueListener(ArithmeticForwardGame game) {
            this.game = game;
        }

        @Override
        public void setValue(Node node, int previousValue, int newValue) {
            if (newValue == game.getExpectedSolution()) {
                found = game.getStep();
                game.stopExecution();
            }
        }
    }

    private class ValueDrawingPeek5 extends ValueDrawingHp {

        public static void registerHyperparameters(HyperManager hyperManager) {
            hyperManager.get(HYPER_FRAC_PORTALS);
            ValueDrawingHp.registerHyperparameter(hyperManager);
        }

        public ValueDrawingPeek5(HyperManager hyperManager) {
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

    public record Solution(StagePeek4.Solution upstreamPeek4,
                           long netAddOnSeed,
                           long mixerSeed,
                           CheckTargetValueListener checkTargetValueListener) {
    }
}
