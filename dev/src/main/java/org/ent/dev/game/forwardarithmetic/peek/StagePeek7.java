package org.ent.dev.game.forwardarithmetic.peek;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.NopEntEventListener;
import org.ent.dev.game.forwardarithmetic.ArithmeticForwardGame;
import org.ent.dev.game.forwardarithmetic.StageBase;
import org.ent.dev.randnet.RandomNetCreator;
import org.ent.dev.randnet.ValueDrawing;
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
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.operation.TriOperation;
import org.ent.net.util.NetCopy2;
import org.ent.net.util.RandomUtil;
import org.ent.run.StepResult;
import org.ent.webui.WebUI;
import org.ent.webui.WebUiStoryOutput;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

public class StagePeek7 extends StageBase<StagePeek7.Solution> {

    private static final boolean WEB_UI = true;
    public static final boolean REPLAY_HITS = false || WEB_UI;
    private static final boolean ANNOTATIONS = false || WEB_UI;

    public static final IntHyperDefinition HYPER_MAX_ATTEMPTS = new IntHyperDefinition("max-attempts", 1, 20_000);
    public static final IntHyperDefinition HYPER_MAX_STEPS = new IntHyperDefinition("max-steps", 3, 100);
    public static final IntHyperDefinition HYPER_NO_NODES_ADD_ON = new IntHyperDefinition("no-nodes-add-on", 1, 40);
    public static final DoubleHyperDefinition HYPER_ARROW_MIX_STRENGTH_TARGET = new DoubleHyperDefinition("arrow-mix-strength-target", 0.0, 1.5);
    public static final DoubleHyperDefinition HYPER_ARROW_MIX_STRENGTH = new DoubleHyperDefinition("arrow-mix-strength", 0.0, 1.0);
    public static final DoubleHyperDefinition HYPER_ARROW_MIX_STRENGTH2 = new DoubleHyperDefinition("arrow-mix-strength2", 0.0, 1.0);

    public static final String HYPER_GROUP_STAGE6 = "stage6";
    public static final String HYPER_GROUP_STAGE7_DRAWING = "stage7-drawing";

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

    private final StagePeek6 stagePeek6;

    private int indexAttempt;

    public StagePeek7(HyperManager hyperManager, UniformRandomProvider randMaster) {
        super(randMaster);

        this.drawing = new ValueDrawingWithPortalAndEvalFlow(hyperManager.group(HYPER_GROUP_STAGE7_DRAWING));

        this.maxAttempts = hyperManager.get(HYPER_MAX_ATTEMPTS);
        this.maxSteps = hyperManager.get(HYPER_MAX_STEPS);
        this.numberOfNodesAddOn = hyperManager.get(HYPER_NO_NODES_ADD_ON);
        this.arrowMixStrengthTarget = hyperManager.get(HYPER_ARROW_MIX_STRENGTH_TARGET);
        this.arrowMixStrength = hyperManager.get(HYPER_ARROW_MIX_STRENGTH);
        this.arrowMixStrength2 = hyperManager.get(HYPER_ARROW_MIX_STRENGTH2);

        this.randNetSeeds = RandomUtil.newRandom2(randMaster.nextLong());
        this.randMixerSeeds = RandomUtil.newRandom2(randMaster.nextLong());
        this.randTargets = RandomUtil.newRandom2(randMaster.nextLong());

        this.stagePeek6 = new StagePeek6(
                hyperManager.group(HYPER_GROUP_STAGE6),
                RandomUtil.newRandom2(randMaster.nextLong()));
    }

    public static void main(String[] args) throws IOException {
        if (WEB_UI) {
            WebUI.setUpJavalin();
        }
        new StagePeek7.Factory().main(1000);
    }

    static class Factory extends StageFactory<StagePeek7> {
        @Override
        protected String getStudyName() {
            return super.getStudyName() + "_2";
        }

        @Override
        public StagePeek7 createStage(RemoteHyperManager hyperManager, int indexTrial) {
            long masterSeed = randomTrials.nextLong();
            masterSeed = -769137859573100126L;
            log.info("using master seed {} for trial {}", masterSeed, indexTrial);
            StagePeek7 dev = new StagePeek7(hyperManager, RandomUtil.newRandom2(masterSeed));
//            dev.setTrialMaxEvaluations(100);
            dev.setTrialMaxDuration(Duration.ofSeconds(20));
            return dev;
        }

        @Override
        public void registerHyperparameters(HyperManager hyperCollector) {
            new StagePeek6.Factory().registerHyperparameters(hyperCollector.group(HYPER_GROUP_STAGE6));
            ValueDrawingWithPortalAndEvalFlow.registerHyperparameters(hyperCollector.group(HYPER_GROUP_STAGE7_DRAWING));
            hyperCollector.get(HYPER_MAX_ATTEMPTS);
            hyperCollector.get(HYPER_MAX_STEPS);
            hyperCollector.get(HYPER_NO_NODES_ADD_ON);
            hyperCollector.get(HYPER_ARROW_MIX_STRENGTH_TARGET);
            hyperCollector.get(HYPER_ARROW_MIX_STRENGTH);
            hyperCollector.get(HYPER_ARROW_MIX_STRENGTH2);
        }

        @Override
        public void fixHyperparameters(HyperManager hyperManager) {
            new StagePeek6.Factory().fixHyperparameters(hyperManager.group(HYPER_GROUP_STAGE6));
            hyperManager.fix(HYPER_MAX_ATTEMPTS, 2300);
            hyperManager.fix(HYPER_MAX_STEPS, 30);
            hyperManager.fix(HYPER_NO_NODES_ADD_ON, 30);
            hyperManager.fix(HYPER_ARROW_MIX_STRENGTH_TARGET, 0.9);
            hyperManager.fix(HYPER_ARROW_MIX_STRENGTH, 0.2);
            hyperManager.fix(HYPER_ARROW_MIX_STRENGTH2, 0.15);
            hyperManager.group(HYPER_GROUP_STAGE7_DRAWING).fixLines("""
                    fraction_commands 0.9
                    fraction_major_commands 0.9
                    fraction_major_split 0.1
                    fraction_portals 0.2
                    fraction_set 0.5
                    fraction_eval_flow 0.9
                    """);
        }
    }


    @Override
    protected void nextEvaluation() {
        StagePeek6.Solution upstreamPeek6 = stagePeek6.getNextSolution();
        Net net0 = upstreamPeek6.prepareNet();

        for (int indexAttempt = 0; indexAttempt < maxAttempts; indexAttempt++) {
            long netAddOnSeed = randNetSeeds.nextLong();
            long mixerSeed = randMixerSeeds.nextLong();
            ArithmeticForwardGame game = setUpGame(net0, upstreamPeek6, netAddOnSeed, mixerSeed);

            FinishedChecker finishedChecker = new FinishedChecker(game);
            game.getEnt().addEventListener(finishedChecker);

            game.execute();

            boolean hit = false;
            boolean maybeHit = finishedChecker.found != null;
            Solution solution = null;
            if (maybeHit) {
                this.indexAttempt = indexAttempt;
                solution = new Solution(net0, upstreamPeek6, netAddOnSeed, mixerSeed);
                if (verify(solution)) {
                    hit = true;
                }
            }
            if (hit) {
                submitSolution(solution);
                if (REPLAY_HITS) {
                    Solution solution_final = solution;
                    log.info("HIT!!!!");
                    WebUiStoryOutput.addStoryWithAnnouncement("StagePeek7-%s-%s-%s".formatted(indexTrial, indexEvaluation, indexAttempt),
                            () -> replayWithDetails(solution_final));

                }
                numHit++;
                break;
            }
        }
    }

    private boolean verify(Solution solution) {
        for (int i = 0; i < 10; i++) {
            boolean verified = WebUiStoryOutput.runStoryWithAnnouncement(
                    "StagePeek7-t%s-e%s-a%s-v%s".formatted(indexTrial, indexEvaluation, indexAttempt, i),
                    () -> verifySingle(solution));
            if (!verified) {
                log.info("Verfication failed! {}", i);
                return false;
            }
        }
        return true;
    }

    private boolean verifySingle(Solution solution) {
        int operand1 = ArithmeticForwardGame.drawOperand(randTargets);
        int operand2 = ArithmeticForwardGame.drawOperand(randTargets);
        ArithmeticForwardGame game0 = solution.upstreamPeek6().upstreamPeek5b().upstreamPeek4().upstreamPeek3().upstreamPeek1().game();
        TriOperation operation;
        do {
            operation = ArithmeticForwardGame.drawOperation(randTargets);
        } while (operation == game0.getOperation());

        ArithmeticForwardGame game = setUpGame(
                solution.net0(), solution.upstreamPeek6(), solution.netAddOnSeed(), solution.mixerSeed(),
                operand1, operand2, operation);

        FinishedChecker finishedChecker = new FinishedChecker(game);
        game.getEnt().addEventListener(finishedChecker);

        if (WEB_UI) {
            game.setVerbose(true);
        }

        game.execute();
        return finishedChecker.found != null;
    }


    private void replayWithDetails(Solution solution) {
        TrimmingListener trimmingListener;
        {
            ArithmeticForwardGame game = setUpGame(
                    solution.net0(),
                    solution.upstreamPeek6(),
                    solution.netAddOnSeed(),
                    solution.mixerSeed());
            FinishedChecker finishedChecker = new FinishedChecker(game);
            game.getEnt().addEventListener(finishedChecker);
            trimmingListener = new TrimmingListener(game.getEnt().getNet().getNodes().size());
            game.getEnt().getNet().addEventListener(trimmingListener);

            game.execute();
        }
        {
            ArithmeticForwardGame game = setUpGame(
                    solution.net0(),
                    solution.upstreamPeek6(),
                    solution.netAddOnSeed(),
                    solution.mixerSeed());
            TrimmingHelper.trim(game.getEnt().getNet(), trimmingListener);

            FinishedChecker finishedChecker = new FinishedChecker(game);
            game.getEnt().addEventListener(finishedChecker);

            game.setVerbose(true);

            game.execute();
        }
    }

    private ArithmeticForwardGame setUpGame(Net net0, StagePeek6.Solution upstreamPeek6, long netAddOnSeed, long mixerSeed) {
        ArithmeticForwardGame game0 = upstreamPeek6.upstreamPeek5b().upstreamPeek4().upstreamPeek3().upstreamPeek1().game();
        int operand1 = game0.getOperand1();
        int operand2 = game0.getOperand2();
        TriOperation operation = game0.getOperation();

        return setUpGame(net0, upstreamPeek6, netAddOnSeed, mixerSeed, operand1, operand2, operation);
    }

    @NotNull
    private ArithmeticForwardGame setUpGame(Net net0, StagePeek6.Solution upstreamPeek6, long netAddOnSeed, long mixerSeed, int operand1, int operand2, TriOperation operation) {
        Net net = setUpNet(net0, upstreamPeek6, netAddOnSeed, mixerSeed);

        int realMaxSteps = upstreamPeek6.rightAnswerListener().foundStep + 1 + maxSteps;
        ArithmeticForwardGame game = new ArithmeticForwardGame(
                operand1,
                operand2,
                operation,
                net,
                realMaxSteps);
        game.initializeVerifier();
        return game;
    }

    @NotNull
    private Net setUpNet(Net net0, StagePeek6.Solution upstreamPeek6, long netAddOnSeed, long mixerSeed) {
        Net net = NetCopy2.createCopy(net0);
        int previousSize = net.getNodes().size();

        RandomNetCreator netCreator = new RandomNetCreator(numberOfNodesAddOn, RandomUtil.newRandom2(netAddOnSeed), drawing);
        Net netAddOn = netCreator.drawNet();

        Node addOnRoot = netAddOn.getRoot();
        List<Node> netAddOnNodes = netAddOn.removeAllNodes();
        net.addNodes(netAddOnNodes);
        if (ANNOTATIONS) {
            for (Node netAddOnNode : netAddOnNodes) {
                net.appendAnnotation(netAddOnNode, "@7");
            }
        }
        Node stitchOriginAddOn = net.getNode(upstreamPeek6.rightAnswerListener().foundTarget.getIndex());
        stitchOriginAddOn.setRightChild(addOnRoot, Purview.DIRECT);

        {
            Node target = upstreamPeek6.rightAnswerListener().foundTarget;
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

    public record Solution(Net net0, StagePeek6.Solution upstreamPeek6, long netAddOnSeed, long mixerSeed) {
    }

    private static class FinishedChecker extends NopEntEventListener {

        private final ArithmeticForwardGame game;
        public Integer found;

        private FinishedChecker(ArithmeticForwardGame game) {
            this.game = game;
        }

        @Override
        public void afterCommandExecution(StepResult stepResult) {
            if (game.getVerifierNet().getRoot().getValue(Purview.DIRECT) == Commands.FINAL_SUCCESS.getValue()) {
                found = game.getStep();
                game.stopExecution();
            } else if (game.getVerifierNet().getRoot().getValue(Purview.DIRECT) == Commands.FINAL_FAILURE.getValue()) {
                game.stopExecution();
            }
        }
    }

}
