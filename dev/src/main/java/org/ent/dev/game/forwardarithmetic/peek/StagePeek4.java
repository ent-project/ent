package org.ent.dev.game.forwardarithmetic.peek;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.dev.game.forwardarithmetic.ArithmeticForwardGame;
import org.ent.dev.game.forwardarithmetic.ArithmeticForwardGame.OpTarget;
import org.ent.dev.game.forwardarithmetic.ReadOperandsEntListener;
import org.ent.dev.game.forwardarithmetic.StageBase;
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
import org.ent.net.util.NetCopy2;
import org.ent.net.util.NetCopyPack;
import org.ent.net.util.NetUtils;
import org.ent.net.util.RandomUtil;
import org.ent.webui.WebUI;
import org.ent.webui.WebUiStoryOutput;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Set;

/**
 * Stitch the fragments together, such that after execution, all op-values (operation, operator1, operator2)
 * are present in the Net (or the answer domain).
 */
public class StagePeek4 extends StageBase<StagePeek4.Solution> {

    private static final boolean WEB_UI = false;
    public static final boolean REPLAY_HITS = false || WEB_UI;

    public static final IntHyperDefinition HYPER_MAX_STEPS = new IntHyperDefinition("max-steps", 3, 200);
    public static final IntHyperDefinition HYPER_MAX_ATTEMPTS = new IntHyperDefinition("max-attempts", 1, 400);
    public static final IntHyperDefinition HYPER_FRAGMENT_CONTEXT = new IntHyperDefinition("fragment-context", 1, 20);
    public static final IntHyperDefinition HYPER_LOOSE_STITCHING = new IntHyperDefinition("loose-stitching", 0, 1);
    public static final DoubleHyperDefinition HYPER_ARROW_MIX_STRENGTH = new DoubleHyperDefinition("arrow-mix-strength", 0.0, 1.0);

    public static final String HYPER_GROUP_STAGE3 = "stage3";

    private final int maxAttempts;
    private final int maxSteps;
    private final int fragmentContext;
    private final boolean looseStitching;
    private final double arrowMixStrength;

    private final StagePeek3 stagePeek3;
    private final UniformRandomProvider randMixerSeeds;

    public StagePeek4(HyperManager hyperManager, UniformRandomProvider randMaster) {
        super(randMaster);

        this.maxSteps = hyperManager.get(HYPER_MAX_STEPS);
        this.maxAttempts = hyperManager.get(HYPER_MAX_ATTEMPTS);
        this.fragmentContext = hyperManager.get(HYPER_FRAGMENT_CONTEXT);
        this.looseStitching = hyperManager.get(HYPER_LOOSE_STITCHING) > 0;
        this.arrowMixStrength = hyperManager.get(HYPER_ARROW_MIX_STRENGTH);

        this.randMixerSeeds = RandomUtil.newRandom2(randMaster.nextLong());

        this.stagePeek3 = new StagePeek3(
                hyperManager.group(HYPER_GROUP_STAGE3),
                RandomUtil.newRandom2(randMaster.nextLong()));
    }

    public static void main(String[] args) throws IOException {
        if (WEB_UI) {
            WebUI.setUpJavalin();
        }
        new StagePeek4.Factory().main(1);
    }

    static class Factory extends StageFactory<StagePeek4> {

        @Override
        public StagePeek4 createStage(RemoteHyperManager hyperManager, int indexTrial) {
            StagePeek4 dev = new StagePeek4(hyperManager, RandomUtil.newRandom2(randomTrials.nextLong()));
            dev.setTrialMaxEvaluations(50);
//            dev.setTrialMaxDuration(Duration.ofSeconds(18));
            return dev;
        }

        @Override
        public void registerHyperparameters(HyperManager hyperCollector) {
            new StagePeek3.Factory().registerHyperparameters(hyperCollector.group(HYPER_GROUP_STAGE3));
            hyperCollector.get(HYPER_LOOSE_STITCHING);
            hyperCollector.get(HYPER_MAX_STEPS);
            hyperCollector.get(HYPER_FRAGMENT_CONTEXT);
            hyperCollector.get(HYPER_MAX_ATTEMPTS);
            hyperCollector.get(HYPER_ARROW_MIX_STRENGTH);
        }

        @Override
        public void fixHyperparameters(HyperManager hyperManager) {
            new StagePeek3.Factory().fixHyperparameters(hyperManager.group(HYPER_GROUP_STAGE3));
            hyperManager.fix(HYPER_LOOSE_STITCHING, 0);
            hyperManager.fix(HYPER_MAX_STEPS, 30);
            hyperManager.fix(HYPER_FRAGMENT_CONTEXT, 18);
            hyperManager.fix(HYPER_MAX_ATTEMPTS, 130);
            hyperManager.fix(HYPER_ARROW_MIX_STRENGTH, 0.45);
        }
    }

    @Override
    protected void nextEvaluation() {
        StagePeek3.Solution upstreamPeek3 = stagePeek3.getNextSolution();
        ArithmeticForwardGame game0 = upstreamPeek3.upstreamPeek1().game();

        StitchResult stitched = stitchTogether(upstreamPeek3);

        for (int indexAttempt = 0; indexAttempt < maxAttempts; indexAttempt++) {
            Net netAttempt = NetCopy2.createCopy(stitched.net());
            long mixerSeed = randMixerSeeds.nextLong();
            applyArrowMixMutation(netAttempt, mixerSeed);
            ArithmeticForwardGame game = new ArithmeticForwardGame(
                    game0.getOperand1(),
                    game0.getOperand2(),
                    game0.getOperation(),
                    netAttempt,
                    maxSteps);
            Peek4ReadOperandsEntListener readOperandsListener = new Peek4ReadOperandsEntListener(game);
            game.getEnt().addEventListener(readOperandsListener);
            game.setAfterStepHook(readOperandsListener);

            game.execute();

            boolean hit = readOperandsListener.allFound != null;
            if (hit) {
                Solution solution = new Solution(
                        stitched.net(),
                        stitched.nextOrigin(),
                        mixerSeed,
                        upstreamPeek3,
                        readOperandsListener);
                submitSolution(solution);
                if (REPLAY_HITS) {
                    WebUiStoryOutput.addStoryWithAnnouncement("StagePeek4-%s-%s".formatted(indexTrial, indexEvaluation),
                            () -> replayWithDetails(solution));
                }
                numHit++;
                break;
            }
        }
    }

    public void applyArrowMixMutation(Net netAttempt, long mixerSeed) {
        ArrowMixMutation mixMutation = new ArrowMixMutation(arrowMixStrength, netAttempt, RandomUtil.newRandom2(mixerSeed));
        mixMutation.execute();
    }

    record StitchResult(Net net, Node nextOrigin) {

    }
    private record StitchContribution(StagePeek3.Fragment fragment, Net condensedNet) {
    }

    private StitchResult stitchTogether(StagePeek3.Solution upstreamPeek3) {
        StitchContribution[] contributions = new StitchContribution[upstreamPeek3.fragments().size()];
        for (int i = 0; i < upstreamPeek3.fragments().size(); i++) {
            StagePeek3.Fragment fragment = upstreamPeek3.fragments().get(i);
            Net netFragment = concentrate(upstreamPeek3, fragment);

            contributions[i] = new StitchContribution(fragment, netFragment);
        }

        Net net = new NetCopyPack(upstreamPeek3.upstreamPeek1().net()).createCopy();
        Node stitchOrigin = net.getRoot();
        if (looseStitching) {
            stitchOrigin = stitchOrigin.getRightChild(Purview.DIRECT);
        }

        for (StitchContribution contribution : contributions) {
            NetCopyPack copy = new NetCopyPack(contribution.condensedNet);
            copy.copyIntoExistingNet(net);

            Node stitchTarget = net.getNode(copy.getCopiedNodeIndex(contribution.condensedNet.getRoot()));

            stitchOrigin.setRightChild(stitchTarget, Purview.DIRECT);

            // determine stitch origin for the next iteration
            stitchOrigin = stitchTarget;
            int numDescent = looseStitching ? fragmentContext : fragmentContext - 1;
            for (int i = 0; i < numDescent; i++) {
                stitchOrigin = stitchOrigin.getRightChild(Purview.DIRECT);
            }
        }
        return new StitchResult(net, stitchOrigin);
    }

    private Net concentrate(StagePeek3.Solution solution, StagePeek3.Fragment fragment) {
        Net net = stagePeek3.buildReadNet(fragment.netSeed());

        ArithmeticForwardGame game = stagePeek3.stagePeek2().setUpGame(solution.upstreamPeek1, net);
        ReadOperandsEntListener readOperandsListener = fragment.readOperandsEntListener();
        game.setMaxSteps(readOperandsListener.data(fragment.contributesToTarget()).firstTransfer);

        game.execute();

        Net advancedNet = NetCopy2.createCopy(net);

        TrimmingListener trimmingListener = new TrimmingListener(net.getNodes().size());
        net.addEventListener(trimmingListener);
        game.setMaxSteps(game.getMaxSteps() + fragmentContext);
        game.execute();

        TrimmingHelper.trim(advancedNet, trimmingListener);
        return advancedNet;
    }

    private void replayWithDetails(Solution solution) {
        ArithmeticForwardGame game0 = solution.upstreamPeek3().upstreamPeek1().game();

        Net net = NetCopy2.createCopy(solution.net());
        applyArrowMixMutation(net, solution.mixerSeed);

        ArithmeticForwardGame game = new ArithmeticForwardGame(
                game0.getOperand1(),
                game0.getOperand2(),
                game0.getOperation(),
                net,
                solution.readOperandsListener().allFound + 1);
        game.setVerbose(true);

        game.execute();
    }

    private class Peek4ReadOperandsEntListener extends ReadOperandsEntListener implements ArithmeticForwardGame.AfterStepHook {

        Integer allFound;

        public Peek4ReadOperandsEntListener(ArithmeticForwardGame game) {
            super(game);
        }

        @Override
        public void afterTransferHook(TransferData data) {
            if (operand1Data.numTransfer > 0 && operand2Data.numTransfer > 0 && operationData.numTransfer > 0) {
                game.submitAfterStepInfo(data);
            }
        }

        public void afterStep(ArithmeticForwardGame game, Object ignored) {
            Set<Node> nodes = NetUtils.collectReachable(game.getEnt().getNet().getRoot());
            nodes.add(game.getAnswerNode());
            EnumMap<OpTarget, Node> found = new EnumMap<>(OpTarget.class);
            int numFound = 0;
            for (Node node : nodes) {
                for (OpTarget target : OpTarget.values()) {
                    if (found.get(target) != null) {
                        continue;
                    }
                    if (game.getOpValue(target) == node.getValue(Purview.DIRECT)) {
                        found.put(target, node);
                        numFound++;
                    }
                }
                if (numFound >= OpTarget.values().length) {
                    allFound = game.getStep();
                    game.stopExecution();
                    break;
                }
            }
        }
    }

    public record Solution(
            Net net,
            Node nextStitchOrigin,
            long mixerSeed,
            StagePeek3.Solution upstreamPeek3,
            Peek4ReadOperandsEntListener readOperandsListener) {
    }

}
