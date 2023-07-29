package org.ent.dev.game.forwardarithmetic.peek;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.LazyPortalArrow;
import org.ent.NopNetEventListener;
import org.ent.dev.game.forwardarithmetic.ArithmeticForwardGame;
import org.ent.dev.game.forwardarithmetic.PortalMoveEntEventListener;
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
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.operation.Operations;
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
 * Verify, that the upstream solution has a viable follow-up.
 * (Portal 1 has moved to a place where operand-nodes are accessible.)
 */
public class StagePeek2 extends StageBase<StagePeek1.Solution> {

    private static final boolean WEB_UI = false;
    public static final boolean REPLAY_HITS = false || WEB_UI;

    public static final DoubleHyperDefinition HYPER_FRAC_PORTALS = new DoubleHyperDefinition("fraction_portals", 0.0, 1.0);
    public static final IntHyperDefinition HYPER_MAX_STEPS = new IntHyperDefinition("max-steps", 3, 200);
    public static final IntHyperDefinition HYPER_NO_NODES = new IntHyperDefinition("no-nodes", 2, 400);
    public static final IntHyperDefinition HYPER_ATTEMPTS_PER_UPSTREAM = new IntHyperDefinition("attempts-per-upstream", 1, 1000);

    public static final String HYPER_GROUP_STAGE1 = "stage1";

    private static final Logger logStatic = LoggerFactory.getLogger(StagePeek1.class);

    private final int numberOfNodes;
    private final int maxSteps;
    private final int attemptsPerUpstream;

    public final ValueDrawing drawing;

    private final StagePeek1 stagePeek1;
    private final UniformRandomProvider randNetSeeds;

    private int numHit;
    private int numEvaluation;

    public static void registerHyperparameters(HyperManager hyperCollector) {
        StagePeek1.registerHyperparameters(hyperCollector.group(HYPER_GROUP_STAGE1));
        hyperCollector.get(HYPER_FRAC_PORTALS);
        hyperCollector.get(HYPER_MAX_STEPS);
        hyperCollector.get(HYPER_NO_NODES);
        hyperCollector.get(HYPER_ATTEMPTS_PER_UPSTREAM);
    }

    public StagePeek2(HyperManager hyperManager, UniformRandomProvider randMaster) {
        super(randMaster);

        this.drawing = new ValueDrawingPeek2(hyperManager);
        this.maxSteps = hyperManager.get(HYPER_MAX_STEPS);
        this.numberOfNodes = hyperManager.get(HYPER_NO_NODES);
        this.attemptsPerUpstream = hyperManager.get(HYPER_ATTEMPTS_PER_UPSTREAM);

        this.randNetSeeds = RandomUtil.newRandom2(randMaster.nextLong());

        this.stagePeek1 = new StagePeek1(
                hyperManager.group(HYPER_GROUP_STAGE1),
                RandomUtil.newRandom2(randMaster.nextLong()));
    }

    public static void main(String[] args) throws IOException {
        if (WEB_UI) {
            WebUI.setUpJavalin();
        }
        UniformRandomProvider randomRun = RandomUtil.newRandom2(12345L);
        CollectingHyperManager hyperCollector = new CollectingHyperManager();
        StagePeek2.registerHyperparameters(hyperCollector);

        RemoteHyperManager hyperManager = new RemoteHyperManager(hyperCollector.getHyperDefinitions());
        StagePeek1.fixHyperparameters(hyperManager.group(HYPER_GROUP_STAGE1));

        hyperManager.fix(HYPER_FRAC_PORTALS, 0.4);
        hyperManager.fix(HYPER_NO_NODES, 50);
        hyperManager.fix(HYPER_MAX_STEPS, 100);
        hyperManager.fix(HYPER_ATTEMPTS_PER_UPSTREAM, 20);

        for (int indexTrial = 0; indexTrial < 1; indexTrial++) {
            Integer trialNumberRemote = hyperManager.suggest();

            StagePeek2 dev = new StagePeek2(hyperManager, RandomUtil.newRandom2(randomRun.nextLong()));
            dev.setTrialMaxEvaluations(2000);

            dev.runTrial(indexTrial);
            int hits = dev.numHit;
            double hitsPerMinute = hits * 60_000.0 / dev.duration.toMillis();
            logStatic.info(" Hits per minute: " + hitsPerMinute);

            hyperManager.complete(trialNumberRemote, hitsPerMinute);
        }
    }

    @Override
    protected void printRunInfo(Duration duration) {
        log.info("TOTAL DURATION: {}", duration);
        log.info(" hits: {}", Tools.rate(numHit, numEvaluation));
    }

    @Override
    protected void nextEvaluation() {
        StagePeek1.Solution upstream = stagePeek1.getNextSolution();

        boolean foundSolution = false;
        int numGetOperand1 = 0;
        int numGetOperand2 = 0;
        for (int indexAttempt = 0; indexAttempt < attemptsPerUpstream; indexAttempt++) {
            long netSeed = this.randNetSeeds.nextLong();
            Net net = buildReadNet(netSeed);

            ArithmeticForwardGame game = setUpGame(upstream, net);
            ReachOpsNetListener reachOpsListener = new ReachOpsNetListener(game);
            game.getVerifierNet().addEventListener(reachOpsListener);

            game.execute();

            numGetOperand1 += reachOpsListener.numGetOperand1;
            numGetOperand2 += reachOpsListener.numGetOperand2;

            if (numGetOperand1 > 0 && numGetOperand2 > 0) {
                foundSolution = true;
                break;
            }
        }
        if (foundSolution) {
            submitSolution(upstream);
            if (REPLAY_HITS) {
                String storyId = "stage-peek2-%s-%s".formatted(indexTrial, indexEvaluation);
                WebUiStoryOutput.addStory(storyId, () -> {
                    stagePeek1.replayWithDetails(upstream);
                    log.info("replay done.");
                });
                Logging.logHtml(() -> "<a href=\"/?story=%s\" target=\"_blank\">%s</a>".formatted(storyId, storyId));
            }
            numHit++;
        }
        numEvaluation++;
    }

    private Net buildReadNet(Long netSeed) {
        RandomNetCreator netCreator = new RandomNetCreator(numberOfNodes, RandomUtil.newRandom2(netSeed), drawing);
        return netCreator.drawNet();
    }

    private ArithmeticForwardGame setUpGame(StagePeek1.Solution upstream, Net net) {
        ArithmeticForwardGame game0 = upstream.game();
        PortalMoveEntEventListener portalMoves = upstream.portalMoveEntEventListener();
        ArithmeticForwardGame game = new ArithmeticForwardGame(
                game0.getOperand1(),
                game0.getOperand2(),
                game0.getOperation(),
                net,
                this.maxSteps);
        game.initializeVerifier();
        initializePortal(game, portalMoves.targetTracker1(), game.getVerifierPortal1());
        initializePortal(game, portalMoves.targetTracker2(), game.getVerifierPortal2());
        initializeAnswer(game, portalMoves);
        return game;
    }

    private static void initializePortal(ArithmeticForwardGame game, PortalMoveEntEventListener.TargetTracker targetTracker, LazyPortalArrow verifierPortal) {
        Node target = targetTracker.lastTarget();
        if (target != null) {
            verifierPortal.setTarget(game.getVerifierNet().getNode(target.getIndex()), Purview.DIRECT);
        }
    }

    private static void initializeAnswer(ArithmeticForwardGame game, PortalMoveEntEventListener portalMoves) {
        game.getAnswerNode().setValue(portalMoves.lastAnswerValue());
    }


    class ValueDrawingPeek2 extends ValueDrawingHp {

        public ValueDrawingPeek2(HyperManager hyperManager) {
            super(hyperManager);
        }

        @Override
        protected DistributionNode initializeDistribution() {
            double fracPortal = hyperManager.get(HYPER_FRAC_PORTALS);
            log.info("got HPs: fracPortal={}", fracPortal);
            return new DistributionSplit(fracPortal)
                    .first(new DistributionLeaf().add(new PortalValue(0, 0)))
                    .rest(new DistributionLeaf().add(Operations.SET_VALUE_OPERATION));
        }
    }

    private static class ReachOpsNetListener extends NopNetEventListener {
        private final ArithmeticForwardGame game;

        int numGetOperation;
        int numGetOperand1;
        int numGetOperand2;

        private ReachOpsNetListener(ArithmeticForwardGame game) {
            this.game = game;
        }

        @Override
        public void getValue(Node node, Purview purview) {
            if (node == game.getOperationNode()) {
                numGetOperation++;
            } else if (node == game.getOperand1Node()) {
                numGetOperand1++;
            } else if (node == game.getOperand2Node()) {
                numGetOperand2++;
            }
        }
    }
}
