package org.ent.dev.game.forwardarithmetic.peek;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.dev.game.forwardarithmetic.ArithmeticForwardGame;
import org.ent.dev.game.forwardarithmetic.PortalMoveEntEventListener;
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
import org.ent.net.node.cmd.operation.TriOperation;
import org.ent.net.util.NetCopy2;
import org.ent.net.util.NetCopyPack;
import org.ent.net.util.RandomUtil;
import org.ent.util.Logging;
import org.ent.util.Tools;
import org.ent.webui.WebUI;
import org.ent.webui.WebUiStoryOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;

/**
 * Find portal moves and condense them to a single command
 */
public class StagePeek1 extends StageBase<StagePeek1.Solution> {

    private static final boolean WEB_UI = false;
    public static final boolean REPLAY_HITS = false || WEB_UI;

    public static final DoubleHyperDefinition HYPER_FRAC_PORTALS = new DoubleHyperDefinition("fraction_portals", 0.0, 1.0);
    public static final IntHyperDefinition HYPER_MAX_STEPS = new IntHyperDefinition("max-steps", 3, 800);
    public static final IntHyperDefinition HYPER_NO_NODES = new IntHyperDefinition("no-nodes", 2, 1000);

    private static final Logger logStatic = LoggerFactory.getLogger(StagePeek1.class);

    public final ValueDrawing drawing;

    private final int maxSteps;
    private final int numberOfNodes;

    private final UniformRandomProvider randNetSeeds;
    private final UniformRandomProvider randTargets;

    public StagePeek1(HyperManager hyperManager, UniformRandomProvider randMaster) {
        super(randMaster);
        drawing = new ValueDrawingPeek1(hyperManager);
        this.maxSteps = hyperManager.get(HYPER_MAX_STEPS);
        this.numberOfNodes = hyperManager.get(HYPER_NO_NODES);
        this.randNetSeeds = RandomUtil.newRandom2(randMaster.nextLong());
        this.randTargets = RandomUtil.newRandom2(randMaster.nextLong());
    }

    public static void main(String[] args) throws IOException {
        if (WEB_UI) {
            WebUI.setUpJavalin();
        }
        new Factory().main(1);
    }

    public static class Factory extends StageFactory<StagePeek1> {
        @Override
        public StagePeek1 createStage(RemoteHyperManager hyperManager) {
            StagePeek1 dev = new StagePeek1(hyperManager, RandomUtil.newRandom2(randomTrials.nextLong()));
            dev.setTrialMaxEvaluations(200);
//            dev.setTrialMaxDuration(Duration.ofSeconds(10));
            return dev;
        }

        @Override
        public void registerHyperparameters(HyperManager hyperCollector) {
            hyperCollector.get(HYPER_FRAC_PORTALS);
            hyperCollector.get(HYPER_MAX_STEPS);
            hyperCollector.get(HYPER_NO_NODES);
        }

        @Override
        public void fixHyperparameters(HyperManager hyperManager) {
            hyperManager.fix(HYPER_FRAC_PORTALS, 0.4);
            hyperManager.fix(HYPER_NO_NODES, 400);
            hyperManager.fix(HYPER_MAX_STEPS, 80);
        }
    }

    @Override
    protected void printRunInfo(Duration duration) {
        log.info("TOTAL DURATION: {}", duration);
        log.info(" hits: {}", Tools.rate(numHit, numEvaluation));
    }

    @Override
    protected void nextEvaluation() {
        int operand1 = ArithmeticForwardGame.drawOperand(randTargets);
        int operand2 = ArithmeticForwardGame.drawOperand(randTargets);
        TriOperation operation = ArithmeticForwardGame.drawOperation(randTargets);
        long netSeed = randNetSeeds.nextLong();

        ArithmeticForwardGame game = new ArithmeticForwardGame(operand1, operand2, operation, buildNet(netSeed), maxSteps);

        PortalMoveEntEventListener portalMoveListener = new PortalMoveEntEventListener(game);
        portalMoveListener.setExitAfterFirstMove(true);
        game.getEnt().addEventListener(portalMoveListener);

        game.execute();

        boolean hit = portalMoveListener.totalTargetChanges() > 0;
        if (hit) {
            Net solutionNet = concentrate(game, netSeed, portalMoveListener);
            Solution solution = new Solution(solutionNet, game, portalMoveListener);
            submitSolution(solution);
            if (REPLAY_HITS) {
                String storyId = "game-%s-%s".formatted(indexTrial, indexEvaluation);
                WebUiStoryOutput.addStory(storyId, () -> {
                    replayWithDetails(solution);
                    log.info("replay done.");
                });
                Logging.logHtml(() -> "<a href=\"/?story=%s\" target=\"_blank\">%s</a>".formatted(storyId, storyId));
            }
            numHit++;
        }
    }

    private Net concentrate(ArithmeticForwardGame game0, long netSeed, PortalMoveEntEventListener portalMoveListener) {
        Net net = buildNet(netSeed);
        ArithmeticForwardGame game = new ArithmeticForwardGame(
                game0.getOperand1(),
                game0.getOperand2(),
                game0.getOperation(),
                net,
                portalMoveListener.firstTimePortalMoved());
        game.execute();

        Net advancedBlueprint = NetCopy2.createCopy(net);

        TrimmingListener trimmingListener = new TrimmingListener(net.getNodes().size());
        net.addEventListener(trimmingListener);
        game.setMaxSteps(game.getMaxSteps() + 1);
        game.execute();

        TrimmingHelper.trim(advancedBlueprint, trimmingListener);
        return advancedBlueprint;
    }

    public void replayWithDetails(Solution solution) {
        ArithmeticForwardGame game0 = solution.game();
        Net net = new NetCopyPack(solution.net()).createCopy();

        ArithmeticForwardGame game = new ArithmeticForwardGame(
                game0.getOperand1(),
                game0.getOperand2(),
                game0.getOperation(),
                net,
                1);
        game.setVerbose(true);
        game.execute();
    }

    public Net buildNet(Long netSeed) {
        RandomNetCreator netCreator = new RandomNetCreator(numberOfNodes, RandomUtil.newRandom2(netSeed), drawing);
        return netCreator.drawNet();
    }

    class ValueDrawingPeek1 extends ValueDrawingHp {

        public ValueDrawingPeek1(HyperManager hyperManager) {
            super(hyperManager);
        }

        @Override
        protected DistributionNode initializeDistribution() {
            double fracPortal = hyperManager.get(HYPER_FRAC_PORTALS);
            log.info("got HPs: fracPortal={}", fracPortal);
            return new DistributionSplit(fracPortal)
                    .first(new DistributionLeaf().add(new PortalValue(0, 0)))
                    .rest(new DistributionLeaf().add(Operations.SET_OPERATION));
        }
    }

    public record Solution(
            Net net,
            ArithmeticForwardGame game,
            PortalMoveEntEventListener portalMoveEntEventListener) {
    }
}
