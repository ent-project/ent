package org.ent.dev.game.forwardarithmetic.readinfo;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.dev.game.forwardarithmetic.ArithmeticForwardGame;
import org.ent.dev.game.forwardarithmetic.ReadOperandsEntListener;
import org.ent.dev.game.forwardarithmetic.StageBase;
import org.ent.dev.randnet.PortalValue;
import org.ent.dev.randnet.RandomNetCreator;
import org.ent.dev.randnet.ValueDrawing;
import org.ent.dev.randnet.ValueDrawingHp;
import org.ent.hyper.CollectingHyperManager;
import org.ent.hyper.DoubleHyperDefinition;
import org.ent.hyper.HyperManager;
import org.ent.hyper.IntHyperDefinition;
import org.ent.hyper.RemoteHyperManager;
import org.ent.net.Net;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.net.node.cmd.operation.TriOperation;
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
 * Success is transfer of either operand.
 *
 * Implies portal move to reach the operand.
 */
public class StageTransfer1 extends StageBase {

    private static final boolean WEB_UI = false;
    public static final boolean REPLAY_HITS = false || WEB_UI;

    public static final DoubleHyperDefinition HYPER_FRAC_PORTALS = new DoubleHyperDefinition("fraction_portals", 0.0, 1.0);
    public static final IntHyperDefinition HYPER_MAX_STEPS = new IntHyperDefinition("max-steps", 3, 800);
    public static final IntHyperDefinition HYPER_NO_NODES = new IntHyperDefinition("no-nodes", 2, 1000);

    private static final Logger logStatic = LoggerFactory.getLogger(StageTransfer1.class);

    public final ValueDrawing drawing;

    private final int maxSteps;
    private final int numberOfNodes;

    private final UniformRandomProvider randNetSeeds;
    private final UniformRandomProvider randTargets;

    private int numHit;
    private int numEvaluation;

    public StageTransfer1(HyperManager hyperManager, UniformRandomProvider randMaster) {
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
        UniformRandomProvider randomRun = RandomUtil.newRandom2(12345L);

        CollectingHyperManager hyperCollector = new CollectingHyperManager();
        StageTransfer1.registerHyperparameter(hyperCollector);

        RemoteHyperManager hyperManager = new RemoteHyperManager(hyperCollector.getHyperDefinitions());
        hyperManager.fix(HYPER_FRAC_PORTALS, 0.4);
        hyperManager.fix(ValueDrawingHp.FRAC_SET, 0.65);
        hyperManager.fix(HYPER_NO_NODES, 400);
        hyperManager.fix(HYPER_MAX_STEPS, 80);

        for (int indexTrial = 0; indexTrial < 200; indexTrial++) {
            Integer trialNumberRemote = hyperManager.suggest();

            StageTransfer1 dev = new StageTransfer1(hyperManager, RandomUtil.newRandom2(randomRun.nextLong()));
//            dev.setTrialMaxEvaluations(800_000);
            dev.setTrialMaxDuration(Duration.ofSeconds(10));

            dev.runTrial(indexTrial);
            int hits = dev.numHit;
            double hitsPerMinute = hits * 60_000.0 / dev.duration.toMillis();
            logStatic.info(" Hits per minute: " + hitsPerMinute);

            hyperManager.complete(trialNumberRemote, hitsPerMinute);
        }
    }

    public static void registerHyperparameter(HyperManager hyperManager) {
        hyperManager.get(HYPER_FRAC_PORTALS);
        hyperManager.get(ValueDrawingHp.FRAC_SET);
        hyperManager.get(HYPER_MAX_STEPS);
        hyperManager.get(HYPER_NO_NODES);
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

        ReadOperandsEntListener transferListener = new ReadOperandsEntListener(game);
        game.getEnt().addEventListener(transferListener);

        // PortalMoveEntEventListener

        game.execute();

        boolean hit = transferListener.operand1Data().numTransfer > 0
                      || transferListener.operand2Data().numTransfer > 0;
//                      || transferListener.operationData().numTransfer > 0;
        if (hit) {
            log.info("#{} -  o1: {}, op: {}, o2: {}",
                    this.indexEvaluation,
                    transferListener.operand1Data().numTransfer,
                    transferListener.operationData().numTransfer,
                    transferListener.operand2Data().numTransfer);
            if (REPLAY_HITS) {
                String storyId = "game-%s-%s".formatted(indexTrial, indexEvaluation);
                WebUiStoryOutput.addStory(storyId, () -> {
                    replayWithDetails(game, netSeed);
                    log.info("replay done.");
                });
                Logging.logHtml(() -> "<a href=\"/?story=%s\" target=\"_blank\">%s</a>".formatted(storyId, storyId));
            }
            numHit++;
        }
        numEvaluation++;
    }

    private void replayWithDetails(ArithmeticForwardGame game0, long netSeed) {
        ArithmeticForwardGame game = new ArithmeticForwardGame(
                game0.getOperand1(),
                game0.getOperand2(),
                game0.getOperation(),
                buildNet(netSeed),
                maxSteps);
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
            double fracSet = hyperManager.get(FRAC_SET);
            log.info("got HPs: fracPortal={}, fracSet={}", fracPortal, fracSet);
            return new DistributionSplit(fracPortal)
                    .first(new DistributionLeaf().add(new PortalValue(0, 1)))
                    .rest(new DistributionSplit(fracSet)
                            .first(new DistributionLeaf().add(Operations.SET_OPERATION))
                            .rest(new DistributionLeaf().add(Operations.SET_VALUE_OPERATION)));
        }
    }
}
