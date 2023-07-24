package org.ent.dev.game.forwardarithmetic;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.LazyPortalArrow;
import org.ent.NopNetEventListener;
import org.ent.dev.randnet.RandomNetCreator;
import org.ent.dev.randnet.ValueDrawing;
import org.ent.dev.trim2.TrimmingHelper;
import org.ent.dev.trim2.TrimmingListener;
import org.ent.hyper.FixedHyperManager;
import org.ent.hyper.HyperManager;
import org.ent.hyper.IntHyperDefinition;
import org.ent.hyper.PropertyNameResolver;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.util.RandomUtil;
import org.ent.util.Logging;
import org.ent.util.Tools;
import org.ent.webui.WebUI;
import org.ent.webui.WebUiStoryOutput;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class StageReadInfo2b extends StageBase {

    private static final boolean WEB_UI = true;
    public static final boolean REPLAY_HITS = false || WEB_UI;

    private static final Logger logStatic = LoggerFactory.getLogger(StageReadInfo2b.class);

    public static final String HYPER_GROUP_STAGE1 = "stage1";
    public static final String HYPER_GROUP_THIS = "get-value";

    private final int numberOfNodes;
    private final int maxSteps;
    private final int attemptsPerUpstream;

    private final StageReadInfo1 stageReadInfo1;

    private final UniformRandomProvider randNetSeeds;

    public final ValueDrawing drawing;

    private int[] statFirstEvalFlowOnVerifier;

    public static IntHyperDefinition HYPER_MAX_STEPS = new IntHyperDefinition("max-steps", 3, 200);
    public static IntHyperDefinition HYPER_NO_NODES = new IntHyperDefinition("no-nodes", 2, 100);
    public static IntHyperDefinition HYPER_ATTEMPTS_PER_UPSTREAM = new IntHyperDefinition("attempts-per-upstream", 1, 1000);

    private int numTotal;
    private int numGetOperand1BeforeEval;
    private int numGetOperand2BeforeEval;
    private int numGetAnyOperandBeforeEval;
    private int numGetBothOperandsBeforeEval;
    private int numHit;
    private int numUpstream;

    public static void registerHyperparameter(HyperManager hyperCollector) {
        HyperManager hyperCollectorThis = hyperCollector.group(HYPER_GROUP_THIS);
        HyperManager hyperCollectorStage1 = hyperCollector.group(HYPER_GROUP_STAGE1);

        StageReadInfo1.registerHyperparameter(hyperCollectorStage1);
        ValueDrawingWithPortals.registerHyperparameter(hyperCollectorThis);
        hyperCollectorThis.get(HYPER_MAX_STEPS);
        hyperCollectorThis.get(HYPER_NO_NODES);
        hyperCollectorThis.get(HYPER_ATTEMPTS_PER_UPSTREAM);
    }

    public StageReadInfo2b(HyperManager hyperManager, UniformRandomProvider randMaster) {
        super(randMaster);
        HyperManager hyperManagerThis = hyperManager.group(HYPER_GROUP_THIS);
        HyperManager hyperManagerStage1 = hyperManager.group(HYPER_GROUP_STAGE1);

        this.drawing = new ValueDrawingWithPortals(hyperManagerThis);
        this.maxSteps = hyperManagerThis.get(HYPER_MAX_STEPS);
        this.numberOfNodes = hyperManagerThis.get(HYPER_NO_NODES);
        this.attemptsPerUpstream = hyperManagerThis.get(HYPER_ATTEMPTS_PER_UPSTREAM);
        this.stageReadInfo1 = new StageReadInfo1(hyperManagerStage1, RandomUtil.newRandom2(randMaster.nextLong()));
        this.randNetSeeds = RandomUtil.newRandom2(randMaster.nextLong());
        this.statFirstEvalFlowOnVerifier = new int[maxSteps];
    }

    public static void main(String[] args) {
        if (WEB_UI) {
            WebUI.setUpJavalin();
        }
        UniformRandomProvider randomRun = RandomUtil.newRandom2(12345L);

        FixedHyperManager hyperManager = new FixedHyperManager();
        hyperManager.setParameters(StageReadInfo1.HYPER_SELECTION, new PropertyNameResolver(HYPER_GROUP_STAGE1));
        PropertyNameResolver resolverThis = new PropertyNameResolver(HYPER_GROUP_THIS);
        hyperManager.setParameters("""
            {
              'fraction_portals': 0.2,
              'fraction_commands': 0.9,
              'fraction_major_commands': 0.8,
              'fraction_major_split': 0.5,
              'fraction_set': 0.5
            }
                """, resolverThis);
        hyperManager.setParameter(HYPER_MAX_STEPS, 30, resolverThis);
        hyperManager.setParameter(HYPER_NO_NODES, 53, resolverThis);
        hyperManager.setParameter(HYPER_ATTEMPTS_PER_UPSTREAM, 100, resolverThis);

        int numTrials = 1;
        for (int indexTrial = 0; indexTrial < numTrials; indexTrial++) {
            StageReadInfo2b dev = new StageReadInfo2b(hyperManager, RandomUtil.newRandom2(randomRun.nextLong()));
            dev.setEpochSize(10_000);
            dev.runTrial(indexTrial);
            int hits = dev.numHit;
            double hitsPerMinute = hits * 60_000.0 / dev.duration.toMillis();
            logStatic.info(" Hits per minute: " + hitsPerMinute);
        }
    }

    @Override
    protected void printRunInfo(Duration duration) {
        log.info("    get operand before eval:");
        log.info("          o1: {}", Tools.rate(numGetOperand1BeforeEval, numTotal));
        log.info("          o2: {}", Tools.rate(numGetOperand2BeforeEval, numTotal));
        log.info("          any (o1 or o2): {}", Tools.rate(numGetAnyOperandBeforeEval, numTotal));
        log.info("          both (o1 and o2): {}", Tools.rate(numGetBothOperandsBeforeEval, numTotal));
        log.info("TOTAL DURATION: {}", duration);
        log.info(" hits: {}", Tools.rate(numHit, numUpstream));
    }

    @Override
    protected void next(int indexTrial, int indexEpoch) {
        StageReadInfo1.Solution upstream = this.stageReadInfo1.getNextSolution();

        boolean foundSolution = false;
        for (int indexAttempt = 0; indexAttempt < attemptsPerUpstream; indexAttempt++) {
            long netSeed = this.randNetSeeds.nextLong(); // FIXME: consistent base seed for each next()?
            Net net = buildReadNet(netSeed);

            ArithmeticForwardGame game = setUpGame(upstream, net);
            VerifierNetListener verifierNetListener = new VerifierNetListener(game);
            game.getVerifierNet().addEventListener(verifierNetListener);

            game.execute();

            if (verifierNetListener.numGetOperand1BeforeEval > 0) {
                numGetOperand1BeforeEval++;
            }
            if (verifierNetListener.numGetOperand2BeforeEval > 0) {
                numGetOperand2BeforeEval++;
            }
            if (verifierNetListener.numGetOperand1BeforeEval > 0 || verifierNetListener.numGetOperand2BeforeEval > 0) {
                numGetAnyOperandBeforeEval++;
            }
            if (verifierNetListener.numGetOperand1BeforeEval > 0 && verifierNetListener.numGetOperand2BeforeEval > 0) {
                numGetBothOperandsBeforeEval++;
                foundSolution = true;
                if (REPLAY_HITS) {
                    String storyId = "game-%s-%s-%s".formatted(indexTrial, indexEpoch, indexAttempt);
                    WebUiStoryOutput.addStory(storyId, () -> {
                        replayWithDetails(upstream, netSeed);
                        log.info("replay done.");
                    });
                    Logging.logHtml(() -> "<a href=\"/?story=%s\" target=\"_blank\">%s</a>".formatted(storyId, storyId));
                    System.err.println("");
                }
                break;
            }
            numTotal++;
        }
        if (foundSolution) {
            numHit++;
        }
        numUpstream++;
    }

    private void replayWithDetails(StageReadInfo1.Solution upstream, long netSeed) {
        Net net1 = buildReadNet(netSeed);
        ArithmeticForwardGame game1 = setUpGame(upstream, net1);
        TrimmingListener trimmingListener = new TrimmingListener(net1.getNodes().size());
        net1.addEventListener(trimmingListener);

        game1.execute();

        Net net2 = buildReadNet(netSeed);
        TrimmingHelper.trim(net2, trimmingListener);
        ArithmeticForwardGame game2 = setUpGame(upstream, net2);
        HashEntEventListener hashEntEventListener = new HashEntEventListener(game2.getEnt());
        hashEntEventListener.setOnFirstRepetition(r -> game2.stopExecution());
        game2.getEnt().addEventListener(hashEntEventListener);
        game2.setVerbose(true);

        game2.execute();
    }

    @NotNull
    private ArithmeticForwardGame setUpGame(StageReadInfo1.Solution upstream, Net net) {
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

    private static void initializeAnswer(ArithmeticForwardGame game, PortalMoveEntEventListener portalMoves) {
        game.getAnswerNode().setValue(portalMoves.lastAnswerValue());
    }

    private static void initializePortal(ArithmeticForwardGame game, PortalMoveEntEventListener.TargetTracker targetTracker, LazyPortalArrow verifierPortal) {
        Node target = targetTracker.lastTarget();
        if (target != null) {
            verifierPortal.setTarget(game.getVerifierNet().getNode(target.getIndex()), Purview.DIRECT);
        }
    }

    public Net buildReadNet(Long netSeed) {
        RandomNetCreator netCreator = new RandomNetCreator(numberOfNodes, RandomUtil.newRandom2(netSeed), drawing);
        return netCreator.drawNet();
    }


    private class VerifierNetListener extends NopNetEventListener {

        private final ArithmeticForwardGame game;

        private int numGetValue;
        private int numGetOperation;
        private int numGetOperand1;
        private int numGetOperand2;
        private boolean hasEvaluatedOperation;
        private boolean hasEvalFlowed;
        private int numGetOperand1BeforeEval;
        private int numGetOperand2BeforeEval;

        private VerifierNetListener(ArithmeticForwardGame game) {
            this.game = game;
        }

        @Override
        public void beforeEvalExecution(Node target, boolean flow) {
            if (flow) {
                if (!hasEvalFlowed) {
                    hasEvalFlowed = true;
                    statFirstEvalFlowOnVerifier[game.getStep()]++;
                }
            }
            if (target == game.getOperationNode()) {
                hasEvaluatedOperation = true;
            }
        }

        @Override
        public void getValue(Node node, Purview purview) {
            numGetValue++;
            if (node == game.getOperationNode()) {
                numGetOperation++;
            } else if (node == game.getOperand1Node()) {
                numGetOperand1++;
                if (!hasEvaluatedOperation) {
                    numGetOperand1BeforeEval++;
                }
            } else if (node == game.getOperand2Node()) {
                numGetOperand2++;
                if (!hasEvaluatedOperation) {
                    numGetOperand2BeforeEval++;
                }
            }
        }

        public boolean isAnyOperandBeforeEval() {
            return numGetOperand1BeforeEval > 0 || numGetOperand2BeforeEval > 0;
        }
    }

}
