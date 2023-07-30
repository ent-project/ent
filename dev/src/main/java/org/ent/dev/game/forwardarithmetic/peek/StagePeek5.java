package org.ent.dev.game.forwardarithmetic.peek;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.NopEntEventListener;
import org.ent.NopNetEventListener;
import org.ent.dev.game.forwardarithmetic.ArithmeticForwardGame;
import org.ent.dev.game.forwardarithmetic.StageBase;
import org.ent.dev.variation.ArrowMixMutation;
import org.ent.hyper.DoubleHyperDefinition;
import org.ent.hyper.HyperManager;
import org.ent.hyper.IntHyperDefinition;
import org.ent.hyper.RemoteHyperManager;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.operation.TriValueOperation;
import org.ent.net.util.NetCopy2;
import org.ent.net.util.RandomUtil;
import org.ent.webui.WebUI;
import org.ent.webui.WebUiStoryOutput;

import java.io.IOException;

public class StagePeek5 extends StageBase<StagePeek5.Solution> {

    private static final boolean WEB_UI = true;
    public static final boolean REPLAY_HITS = false || WEB_UI;

    public static final IntHyperDefinition HYPER_MAX_ATTEMPTS = new IntHyperDefinition("max-attempts", 1, 400);
    public static final DoubleHyperDefinition HYPER_ARROW_MIX_STRENGTH = new DoubleHyperDefinition("arrow-mix-strength", 0.0, 1.0);
    public static final IntHyperDefinition HYPER_MAX_STEPS = new IntHyperDefinition("max-steps", 3, 200);

    public static final String HYPER_GROUP_STAGE4 = "stage4";

    private final int maxAttempts;
    private final double arrowMixStrength;
    private final int maxSteps;

    private final StagePeek4 stagePeek4;

    private final UniformRandomProvider randMixerSeeds;

    public StagePeek5(HyperManager hyperManager, UniformRandomProvider randMaster) {
        super(randMaster);

        this.maxAttempts = hyperManager.get(HYPER_MAX_ATTEMPTS);
        this.arrowMixStrength = hyperManager.get(HYPER_ARROW_MIX_STRENGTH);
        this.maxSteps = hyperManager.get(HYPER_MAX_STEPS);

        this.randMixerSeeds = RandomUtil.newRandom2(randMaster.nextLong());

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
        public StagePeek5 createStage(RemoteHyperManager hyperManager) {
            StagePeek5 dev = new StagePeek5(hyperManager, RandomUtil.newRandom2(randomTrials.nextLong()));
            dev.setTrialMaxEvaluations(2000);
//            dev.setTrialMaxDuration(Duration.ofSeconds(18));
            return dev;
        }

        @Override
        public void registerHyperparameters(HyperManager hyperCollector) {
            new StagePeek4.Factory().registerHyperparameters(hyperCollector.group(HYPER_GROUP_STAGE4));
            hyperCollector.get(HYPER_MAX_ATTEMPTS);
            hyperCollector.get(HYPER_MAX_STEPS);
            hyperCollector.get(HYPER_ARROW_MIX_STRENGTH);
        }

        @Override
        public void fixHyperparameters(HyperManager hyperManager) {
            new StagePeek4.Factory().fixHyperparameters(hyperManager.group(HYPER_GROUP_STAGE4));
            hyperManager.fix(HYPER_MAX_ATTEMPTS, 400);
            hyperManager.fix(HYPER_MAX_STEPS, 30);
            hyperManager.fix(HYPER_ARROW_MIX_STRENGTH, 0.4);
        }
    }

    @Override
    protected void nextEvaluation() {
        StagePeek4.Solution upstreamPeek4 = stagePeek4.getNextSolution();
        ArithmeticForwardGame game0 = upstreamPeek4.upstreamPeek3().upstreamPeek1().game();

        // this destroys the unmixed net, but we have no use for it anyway
        Net netUpstream = upstreamPeek4.net();
        stagePeek4.applyArrowMixMutation(netUpstream, upstreamPeek4.mixerSeed());

        for (int indexAttempt = 0; indexAttempt < maxAttempts; indexAttempt++) {
            Net netAttempt = NetCopy2.createCopy(netUpstream);
            long mixerSeed = randMixerSeeds.nextLong();
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
            Peek5EntListener peek5EntListener = new Peek5EntListener(game);
            game.getEnt().addEventListener(peek5EntListener);

            game.execute();

            boolean hit = checkTargetValueListener.found != null;
            hit |= peek5EntListener.found2 != null;
            if (hit) {
                if (peek5EntListener.found2 != null) {
                    log.info("#{}-{} - hit!", indexEvaluation, indexAttempt);
                } else {
                    log.info("#{}-{} - HIT!", indexEvaluation, indexAttempt);
                }
                // FIXME: verify with different op-values
                Solution solution = new Solution(upstreamPeek4, mixerSeed, checkTargetValueListener);
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

    private void replayWithDetails(Solution solution) {
        ArithmeticForwardGame game0 = solution.upstreamPeek4().upstreamPeek3().upstreamPeek1().game();

        Net net = NetCopy2.createCopy(solution.upstreamPeek4().net());
        ArrowMixMutation mixMutation = new ArrowMixMutation(arrowMixStrength, net, RandomUtil.newRandom2(solution.mixerSeed()));
        mixMutation.execute();

        ArithmeticForwardGame game = new ArithmeticForwardGame(
                game0.getOperand1(),
                game0.getOperand2(),
                game0.getOperation(),
                net,
                60);
        game.setVerbose(true);

        game.execute();
        log.info("replay done.");
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

    private class Peek5EntListener extends NopEntEventListener {
        protected final ArithmeticForwardGame game;
        Integer found;
        Integer found2;

        private Peek5EntListener(ArithmeticForwardGame game) {
            this.game = game;
        }

        @Override
        public void beforeCommandExecution(Node executionPointer, Command command) {
            if (command != null && command.getValue() == game.getOperationNodeValue()) {
                found = game.getStep();

            }
        }

        @Override
        public void triValueOperation(Node nodeTarget, Node nodeOperand1, Node nodeOperand2, TriValueOperation operation) {
            if (operation == game.getOperation()) {
                int op1 = nodeOperand1.getValue(Purview.DIRECT);
                int op2 = nodeOperand2.getValue(Purview.DIRECT);
                int match = 0;
                boolean fullmatch = false;
                if (op1 == game.getOperand1() && op2 == game.getOperand2()) {
                    fullmatch = true;
                }
                if (op1 == game.getOperand2() && op2 == game.getOperand1()) {
                    fullmatch = true;
                }

                if (op1 == game.getOperand1() || op1 == game.getOperand2()) {
                    match++;
                }
                if (op2 == game.getOperand1() || op2 == game.getOperand2()) {
                    match++;
                }
                if (match >= 2) {
//                    log.info("# match: {}", match);
                }
                if (fullmatch) {
                    log.info("# MATCH: {}", match);
                    found2 = game.getStep();
                    game.stopExecution();
                }
            }
        }
    }

    public record Solution(StagePeek4.Solution upstreamPeek4, long mixerSeed,
                           CheckTargetValueListener checkTargetValueListener) {
    }
}
