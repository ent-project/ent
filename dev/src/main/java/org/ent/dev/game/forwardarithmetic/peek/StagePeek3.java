package org.ent.dev.game.forwardarithmetic.peek;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.dev.game.forwardarithmetic.ArithmeticForwardGame;
import org.ent.dev.game.forwardarithmetic.ArithmeticForwardGame.OpTarget;
import org.ent.dev.game.forwardarithmetic.ReadOperandsEntListener;
import org.ent.dev.game.forwardarithmetic.StageBase;
import org.ent.dev.randnet.PortalValue;
import org.ent.dev.randnet.RandomNetCreator;
import org.ent.dev.randnet.ValueDrawing;
import org.ent.dev.randnet.ValueDrawingHp;
import org.ent.dev.trim2.TrimmingHelper;
import org.ent.dev.trim2.TrimmingListener;
import org.ent.hyper.DoubleHyperDefinition;
import org.ent.hyper.HyperManager;
import org.ent.hyper.IntHyperDefinition;
import org.ent.hyper.RemoteHyperManager;
import org.ent.net.Net;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.net.util.NetCopy2;
import org.ent.net.util.NetCopyPack;
import org.ent.net.util.RandomUtil;
import org.ent.webui.WebUI;
import org.ent.webui.WebUiStoryOutput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Finds 3 independent solutions for the transfer targets (operation, operand1, operand2).
 *
 * Can be stitched together afterwards.
 */
public class StagePeek3 extends StageBase<StagePeek3.Solution> {

    private static final boolean WEB_UI = false;
    public static final boolean REPLAY_HITS = false || WEB_UI;

    private static final int UPSTREAM_MAX_USAGES = 1000;
    public static final DoubleHyperDefinition HYPER_FRAC_PORTALS = new DoubleHyperDefinition("fraction_portals", 0.0, 1.0);
    public static final IntHyperDefinition HYPER_MAX_STEPS = new IntHyperDefinition("max-steps", 3, 200);
    public static final IntHyperDefinition HYPER_NO_NODES = new IntHyperDefinition("no-nodes", 2, 400);

    public static final String HYPER_GROUP_STAGE2 = "stage2";

    private final int numberOfNodes;
    private final int maxSteps;

    private final ValueDrawing drawing;

    private final StagePeek2 stagePeek2;
    private final UniformRandomProvider randNetSeeds;

    private StagePeek1.Solution currentUpstream;
    private int currentUpstreamUsages;

    public StagePeek3(HyperManager hyperManager, UniformRandomProvider randMaster) {
        super(randMaster);

        this.drawing = new ValueDrawingPeek3(hyperManager);

        this.maxSteps = hyperManager.get(HYPER_MAX_STEPS);
        this.numberOfNodes = hyperManager.get(HYPER_NO_NODES);

        this.randNetSeeds = RandomUtil.newRandom2(randMaster.nextLong());
        this.stagePeek2 = new StagePeek2(
                hyperManager.group(HYPER_GROUP_STAGE2),
                RandomUtil.newRandom2(randMaster.nextLong()));
    }

    public StagePeek2 stagePeek2() {
        return stagePeek2;
    }

    public static void main(String[] args) throws IOException {
        if (WEB_UI) {
            WebUI.setUpJavalin();
        }
        new Factory().main(1);
    }

    public static class Factory extends StageFactory<StagePeek3> {
        @Override
        public StagePeek3 createStage(RemoteHyperManager hyperManager) {
            StagePeek3 dev = new StagePeek3(hyperManager, RandomUtil.newRandom2(randomTrials.nextLong()));
            dev.setTrialMaxEvaluations(50);
            return dev;
        }

        @Override
        public void registerHyperparameters(HyperManager hyperCollector) {
            new StagePeek2.Factory().registerHyperparameters(hyperCollector.group(HYPER_GROUP_STAGE2));
            hyperCollector.get(HYPER_FRAC_PORTALS);
            hyperCollector.get(HYPER_MAX_STEPS);
            hyperCollector.get(HYPER_NO_NODES);
        }

        @Override
        public void fixHyperparameters(HyperManager hyperManager) {
            new StagePeek2.Factory().fixHyperparameters(hyperManager.group(HYPER_GROUP_STAGE2));
            hyperManager.fix(HYPER_FRAC_PORTALS, 0.43);
            hyperManager.fix(HYPER_NO_NODES, 50);
            hyperManager.fix(HYPER_MAX_STEPS, 100);
        }
    }

    @Override
    protected void nextEvaluation() {
        StagePeek1.Solution upstream = getUpstream();

        int[] numTransfer = new int[OpTarget.values().length];

        Solution solution = new Solution(upstream);
        int maxAttempts = 1000;
        for (int indexAttempt = 0; indexAttempt < maxAttempts; indexAttempt++) {
            long netSeed = this.randNetSeeds.nextLong();
            Net net = buildReadNet(netSeed);

            ArithmeticForwardGame game = stagePeek2.setUpGame(upstream, net);
            game.setMaxSteps(maxSteps);
            ReadOperandsEntListener readOperandsListener = new ReadOperandsEntListener(game);
            game.getEnt().addEventListener(readOperandsListener);

            game.execute();

            for (var target : OpTarget.values()) {
                if (numTransfer[target.ordinal()] == 0 && readOperandsListener.data(target).numTransfer > 0) {
                    numTransfer[target.ordinal()] += readOperandsListener.data(target).numTransfer;
                    solution.addFragment(new Fragment(game, netSeed, readOperandsListener, target));
                }
            }
            boolean hit = numTransfer[OpTarget.OPERATION.ordinal()] > 0
                          && numTransfer[OpTarget.OPERAND1.ordinal()] > 0
                          && numTransfer[OpTarget.OPERAND2.ordinal()] > 0;
            if (hit) {
                submitSolution(solution);
                if (REPLAY_HITS) {
                    WebUiStoryOutput.addStoryWithAnnouncement("StagePeek3-%s-%s".formatted(indexTrial, indexEvaluation),
                            () -> replayWithDetails(solution));
                }
                numHit++;
                break;
            }
        }
    }

    private StagePeek1.Solution getUpstream() {
        if (currentUpstream != null && currentUpstreamUsages < UPSTREAM_MAX_USAGES) {
            currentUpstreamUsages++;
        } else {
            currentUpstream = stagePeek2.getNextSolution();
            currentUpstreamUsages = 0;
        }
        return currentUpstream;
    }

    public Net buildReadNet(Long netSeed) {
        RandomNetCreator netCreator = new RandomNetCreator(numberOfNodes, RandomUtil.newRandom2(netSeed), drawing);
        return netCreator.drawNet();
    }

    private void replayWithDetails(Solution solution) {
        log.info("number of fragments: " + solution.fragments.size());
        ArithmeticForwardGame game0 = solution.upstreamPeek1.game();
        Net netPortalMove = new NetCopyPack(solution.upstreamPeek1.net()).createCopy();

        ArithmeticForwardGame game = new ArithmeticForwardGame(
                game0.getOperand1(),
                game0.getOperand2(),
                game0.getOperation(),
                netPortalMove,
                1);
        game.setVerbose(true);

        game.execute();

        for (int i = 0; i < solution.fragments.size(); i++) {
            Fragment fragment = solution.fragments.get(i);
            log.info("proceeding with fragment {}, contributing to {}", i, fragment.contributesToTarget);

            Net netFragment = concentrate(solution, fragment);
            game.getEnt().setNet(netFragment);
            game.setMaxSteps(game.getMaxSteps() + 1);

            game.execute();
        }
        log.info("replay done");
    }

    private Net concentrate(Solution solution, Fragment fragment) {
        Net net = buildReadNet(fragment.netSeed());

        ArithmeticForwardGame game = stagePeek2.setUpGame(solution.upstreamPeek1, net);
        ReadOperandsEntListener readOperandsListener = fragment.readOperandsEntListener();
        game.setMaxSteps(readOperandsListener.data(fragment.contributesToTarget()).firstTransfer);

        game.execute();

        Net advancedBlueprint = NetCopy2.createCopy(net);

        TrimmingListener trimmingListener = new TrimmingListener(net.getNodes().size());
        net.addEventListener(trimmingListener);
        game.setMaxSteps(game.getMaxSteps() + 1);
        game.execute();

        TrimmingHelper.trim(advancedBlueprint, trimmingListener);
        return advancedBlueprint;
    }

    private class ValueDrawingPeek3 extends ValueDrawingHp {

        public ValueDrawingPeek3(HyperManager hyperManager) {
            super(hyperManager);
        }

        @Override
        protected DistributionNode initializeDistribution() {
            double fracPortal = hyperManager.get(HYPER_FRAC_PORTALS);
            log.info("got HPs: fracPortal={}", fracPortal);
            PortalValue portal0And1 = new PortalValue(0, 1);
            return new DistributionSplit(fracPortal)
                    .first(new DistributionLeaf().add(portal0And1))
                    .rest(new DistributionLeaf().add(Operations.SET_VALUE_OPERATION));
        }
    }

    public static class Solution {
        final StagePeek1.Solution upstreamPeek1;
        final List<Fragment> fragments = new ArrayList<>();

        public Solution(StagePeek1.Solution upstreamPeek1) {
            this.upstreamPeek1 = upstreamPeek1;
        }

        public StagePeek1.Solution upstreamPeek1() {
            return upstreamPeek1;
        }

        public List<Fragment> fragments() {
            return fragments;
        }

        public void addFragment(Fragment fragment) {
            fragments.add(fragment);
        }
    }

    public record Fragment(
            ArithmeticForwardGame game,
            long netSeed,
            ReadOperandsEntListener readOperandsEntListener, OpTarget contributesToTarget) {
    }

}
