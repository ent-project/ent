package org.ent.dev.game.forwardarithmetic.readinfo;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.NopNetEventListener;
import org.ent.dev.game.forwardarithmetic.ArithmeticForwardGame;
import org.ent.dev.game.forwardarithmetic.PortalMoveEntEventListener;
import org.ent.dev.trim2.TrimmingHelper;
import org.ent.dev.trim2.TrimmingListener;
import org.ent.hyper.FixedHyperManager;
import org.ent.hyper.HyperManager;
import org.ent.hyper.IntHyperDefinition;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.util.NetCopy2;
import org.ent.net.util.NetCopyPack;
import org.ent.net.util.RandomUtil;
import org.ent.util.Logging;
import org.ent.util.Tools;
import org.ent.webui.WebUI;
import org.ent.webui.WebUiStoryOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

/**
 * This demonstrates how to reduce a net to the bare minimum size
 * by identifying the first and last "interesting" step.
 * "Interesting" means it performs the intended modification.
 *
 * 1. Fast-forward to the first interesting step
 * 2. Trim, but only consider usages in steps up to the last interesting step
 *
 * Results:
 * 1. Major reduction in node count in the net.
 * 2. Very first step does the intended action.
 */
public class StageReadInfo2x {

    private static final boolean WEB_UI = true;

    private static final Logger log = LoggerFactory.getLogger(StageReadInfo2x.class);

    public static IntHyperDefinition HYPER_MAX_COMBINATION_ATTEMPTS = new IntHyperDefinition("max-combination-attempts", 1, 400);

    public static void registerHyperparameter(HyperManager hyperManager) {
        hyperManager.get(HYPER_MAX_COMBINATION_ATTEMPTS);
        StageReadInfo1.registerHyperparameter(hyperManager);
    }

    private Integer numEpoch;

    private final StageReadInfo1 stageReadInfo1;

    private final UniformRandomProvider randMaster;

    static class Stats {
        private int numRuns;
        private int numNoRepetition;
        private int[] statFirstRepetition;
        private int numNoEvalFlowOnVerifier;
        private int[] statFirstEvalFlowOnVerifier;
        private int numPortalDidNotMove;
        private int[] statFirstPortalMoved;
        private int[] statNumPortalMoved;
        private int netSize;
        private int[] statNetSize;

        public void recordNetSize(int size) {
            netSize = size;
            if (size < statNetSize.length) {
                statNetSize[size]++;
            }
        }
    }
    private final Stats baseStats = new Stats();
    private final Stats forwardTrimStats = new Stats();
    private final int[] statNetSizePercent;
    private final int[] statRealSteps;

    private Duration duration;

    public static void main(String[] args) {
        if (WEB_UI) {
            WebUI.setUpJavalin();
        }
        UniformRandomProvider randomRun = RandomUtil.newRandom2(12345L);

//        CollectingHyperManager hyperCollector = new CollectingHyperManager();
//        StageReadInfo2.registerHyperparameter(hyperCollector);

        FixedHyperManager hyperManager = new FixedHyperManager();
        hyperManager.fixJson(StageReadInfo1.HYPER_SELECTION);
        hyperManager.fix(HYPER_MAX_COMBINATION_ATTEMPTS, 100);

        for (int i = 0; i < 1; i++) {
            StageReadInfo2x dev = new StageReadInfo2x(hyperManager, RandomUtil.newRandom2(randomRun.nextLong()));
            dev.setNumEpoch(10_000);
            dev.runBatch();
        }
    }

    private void runBatch() {
        long startTime = System.nanoTime();
        for (int i = 1; i <= numEpoch; i++) {
            if (i % 20_000 == 0) {
                log.info("= i={} =", i);
                if (i % 100_000 == 0) {
                    printRunInfo(startTime, baseStats);
                }
            }
            next();
        }
        this.duration = Duration.ofNanos(System.nanoTime() - startTime);
        printRunInfo(startTime, baseStats);
        log.info("====== after forwarding and trimming ======");
        printRunInfo(startTime, forwardTrimStats);
        log.info(" real steps: {}", Arrays.toString(statRealSteps));
        log.info("net size (% of original size after forward and trimming): {}", Arrays.toString(statNetSizePercent));
        log.info(" net size before: {}", Arrays.toString(baseStats.statNetSize));
        log.info(" net size after:  {}", Arrays.toString(forwardTrimStats.statNetSize));
    }

    private void printRunInfo(long startTime, Stats stats) {
        Duration duration = Duration.ofNanos(System.nanoTime() - startTime);
        log.info("total Runs: {}", stats.numRuns);
        log.info("  no eval_flow on verifier: {}", Tools.rate(stats.numNoEvalFlowOnVerifier, stats.numRuns));
        log.info("  first eval_flow on verifier: {}", Arrays.toString(stats.statFirstEvalFlowOnVerifier));
        log.info("  no repetition: {}", Tools.rate(stats.numNoRepetition, stats.numRuns));
        log.info("  first repetitions: {}", Arrays.toString(stats.statFirstRepetition));
        if (stats.numPortalDidNotMove > 0) {
            log.info("    portal did not move: {}", Tools.rate(stats.numPortalDidNotMove, stats.numRuns));
        }
        log.info("  first time portal moved at step: {}", Arrays.toString(stats.statFirstPortalMoved));
        log.info("  number of portal moves: {}", Arrays.toString(stats.statNumPortalMoved));
        log.info("TOTAL DURATION: {}", duration);
    }

    public StageReadInfo2x(HyperManager hyperManager, UniformRandomProvider randMaster) {
        this.randMaster = randMaster;
        this.stageReadInfo1 = new StageReadInfo1(hyperManager, RandomUtil.newRandom2(randMaster.nextLong()));
        initStats(baseStats);
        initStats(forwardTrimStats);
        this.statNetSizePercent = new int[101];
        this.statRealSteps = new int[stageReadInfo1.getMaxSteps() + 1];
    }

    private void initStats(Stats stat) {
        stat.statFirstEvalFlowOnVerifier = new int[stageReadInfo1.getMaxSteps()];
        stat.statFirstRepetition = new int[stageReadInfo1.getMaxSteps() + 1];
        stat.statFirstPortalMoved = new int[stageReadInfo1.getMaxSteps()];
        stat.statNumPortalMoved = new int[stageReadInfo1.getMaxSteps()];
        stat.statNetSize = new int[stageReadInfo1.getNumberOfNodes() * 3 / 2];
    }

    public void setNumEpoch(Integer numEpoch) {
        this.numEpoch = numEpoch;
    }

    private void next() {
        Stats stats = baseStats;
        StageReadInfo1.Solution upstream = this.stageReadInfo1.getNextSolution();
        ArithmeticForwardGame game0 = upstream.game();
        Net net = this.stageReadInfo1.buildNet(upstream.netSeed());
        stats.recordNetSize(net.getNodes().size());
        ArithmeticForwardGame game = new ArithmeticForwardGame(
                game0.getOperand1(),
                game0.getOperand2(),
                game0.getOperation(),
                net,
                game0.getMaxSteps());
        Holder<VerifierNetListener> holder = new Holder<>();
        game.setPostVerifierCreateCallback(verifier ->
                verifier.addEventListener(holder.put(new VerifierNetListener(game, baseStats))));
        HashEntEventListener hashEntEventListener = new HashEntEventListener(game.getEnt());
        game.getEnt().addEventListener(hashEntEventListener);
        PortalMoveEntEventListener entEventListener = new PortalMoveEntEventListener(game);
        game.getEnt().addEventListener(entEventListener);

        game.execute();

        if (hashEntEventListener.firstRepetition == null) {
            stats.numNoRepetition++;
        } else {
            stats.statFirstRepetition[hashEntEventListener.firstRepetition]++;
        }
        if (holder.get() == null || !holder.get().hasEvalFlowed) {
            stats.numNoEvalFlowOnVerifier++;
        }
        stats.statNumPortalMoved[entEventListener.totalTargetChanges()]++;
        if (entEventListener.firstTimePortalMoved() != null) {
            stats.statFirstPortalMoved[entEventListener.firstTimePortalMoved()]++;
        } else {
            throw new AssertionError();
        }
        stats.numRuns++;
        phase2(upstream, entEventListener.firstTimePortalMoved(), entEventListener.lastTimePortalMoved());
    }

    private void phase2(StageReadInfo1.Solution upstream, int firstTimePortalMoved, int lastTimePortalMoved) {
        String uuid = null;
        if (WEB_UI) {
            uuid = UUID.randomUUID().toString();
            if (baseStats.numRuns == 8) {
                System.err.println();
            }
        }

        // move forward and trim
        ArithmeticForwardGame game0 = upstream.game();
        Net net = this.stageReadInfo1.buildNet(upstream.netSeed());
        int stepsForward = firstTimePortalMoved;
        ArithmeticForwardGame gameAdvance = new ArithmeticForwardGame(
                game0.getOperand1(),
                game0.getOperand2(),
                game0.getOperation(),
                net,
                stepsForward
        );

        if (WEB_UI) {
            WebUiStoryOutput.startStory("game-advance-" + uuid);
            gameAdvance.setVerbose(true);
            log.info("run#{}", baseStats.numRuns);
        }
        gameAdvance.execute();
        if (WEB_UI) {
            WebUiStoryOutput.endStory();
        }

        Net advancedBlueprint = NetCopy2.createCopy(net);

        Net advancedNet = NetCopy2.createCopy(advancedBlueprint);

        int realSteps = lastTimePortalMoved - firstTimePortalMoved + 1;
        statRealSteps[realSteps]++;
        ArithmeticForwardGame game = new ArithmeticForwardGame(
                game0.getOperand1(),
                game0.getOperand2(),
                game0.getOperation(),
                advancedNet,
                realSteps);

        TrimmingListener trimmingListener = new TrimmingListener(advancedNet.getNodes().size());
        advancedNet.addEventListener(trimmingListener);
        if (WEB_UI) {
            WebUiStoryOutput.startStory("game-trimming-" + uuid);
            game.setVerbose(true);
            log.info("run#{}", baseStats.numRuns);
        }
        game.execute();
        if (WEB_UI) {
            WebUiStoryOutput.endStory();
        }

        Net advancedNetForTrimming = NetCopy2.createCopy(advancedBlueprint);
        TrimmingHelper.trim(advancedNetForTrimming, trimmingListener);

        Net copy = new NetCopyPack(advancedNetForTrimming).createPackedCopy();

        if (WEB_UI) {
            WebUiStoryOutput.startStory("game-" + uuid);
            log.info("run#{}", baseStats.numRuns);
        }
        phase3(copy, game);
        if (WEB_UI) {
            WebUiStoryOutput.endStory();
            Logging.logHtml("run #%s: <a href=\"/?story=game-advance-%s\" target=\"_blank\">forwarding-process</a> - <a href=\"/?story=game-trimming-%s\" target=\"_blank\">trimming</a> Trimmed and forwarded: <a href=\"/?story=game-%s\" target=\"_blank\">game</a>".formatted(baseStats.numRuns, uuid, uuid, uuid));
            log.info("story recorded.");
        }
    }

    private void phase3(Net net, ArithmeticForwardGame finishedGame) {
        // check out the improved net
        Stats stats = forwardTrimStats;
        stats.recordNetSize(net.getNodes().size());
        ArithmeticForwardGame game = new ArithmeticForwardGame(
                finishedGame.getOperand1(),
                finishedGame.getOperand2(),
                finishedGame.getOperation(),
                net,
                finishedGame.getMaxSteps());
        Holder<VerifierNetListener> holder = new Holder<>();
        game.setPostVerifierCreateCallback(verifier ->
                verifier.addEventListener(holder.put(new VerifierNetListener(game, forwardTrimStats))));
        HashEntEventListener hashEntEventListener = new HashEntEventListener(game.getEnt());
        game.getEnt().addEventListener(hashEntEventListener);
        PortalMoveEntEventListener entEventListener = new PortalMoveEntEventListener(game);
        game.getEnt().addEventListener(entEventListener);

        if (WEB_UI) {
            game.setVerbose(true);
        }

        game.execute();

        if (hashEntEventListener.firstRepetition == null) {
            stats.numNoRepetition++;
        } else {
            stats.statFirstRepetition[hashEntEventListener.firstRepetition]++;
        }
        if (holder.get() == null || !holder.get().hasEvalFlowed) {
            stats.numNoEvalFlowOnVerifier++;
        }
        stats.statNumPortalMoved[entEventListener.totalTargetChanges()]++;
        if (entEventListener.firstTimePortalMoved() != null) {
            stats.statFirstPortalMoved[entEventListener.firstTimePortalMoved()]++;
        } else {
            stats.numPortalDidNotMove++;
        }
        stats.numRuns++;

        int netSizePercentage = forwardTrimStats.netSize * 100 / baseStats.netSize;
        statNetSizePercent[netSizePercentage]++;
    }

    private static class VerifierNetListener extends NopNetEventListener {

        private final ArithmeticForwardGame game;
        private final Stats stats;

        private int numGetValue;
        private int numGetOperation;
        private int numGetOperand1;
        private int numGetOperand2;
        private boolean hasEvaluatedOperation;
        private boolean hasEvalFlowed;
        private int numGetOperand1BeforeEval;
        private int numGetOperand2BeforeEval;

        private VerifierNetListener(ArithmeticForwardGame game, Stats stats) {
            this.game = game;
            this.stats = stats;
        }

        @Override
        public void beforeEvalExecution(Node target, boolean flow) {
            if (flow) {
                if (!hasEvalFlowed) {
                    hasEvalFlowed = true;
                    stats.statFirstEvalFlowOnVerifier[game.getStep()]++;
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
