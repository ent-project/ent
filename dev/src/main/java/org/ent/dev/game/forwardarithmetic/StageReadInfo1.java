package org.ent.dev.game.forwardarithmetic;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.NopEntEventListener;
import org.ent.NopNetEventListener;
import org.ent.dev.randnet.PortalValue;
import org.ent.dev.randnet.RandomNetCreator;
import org.ent.dev.randnet.ValueDrawing;
import org.ent.dev.randnet.ValueDrawingHp;
import org.ent.dev.trim2.TrimmingHelper;
import org.ent.dev.trim2.TrimmingListener;
import org.ent.hyper.CollectingHyperManager;
import org.ent.hyper.DoubleHyperDefinition;
import org.ent.hyper.HyperManager;
import org.ent.hyper.IntHyperDefinition;
import org.ent.hyper.RemoteHyperManager;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.operation.TriOperation;
import org.ent.net.util.RandomUtil;
import org.ent.run.StepResult;
import org.ent.util.Logging;
import org.ent.util.Tools;
import org.ent.webui.WebUI;
import org.ent.webui.WebUiStoryOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class StageReadInfo1 {

    private static final boolean WEB_UI = false;
    public static final boolean REPLAY_HITS = false || WEB_UI;
    public static final boolean TRACK_HASHES = false;

    private static final Logger log = LoggerFactory.getLogger(StageReadInfo1.class);

    public static IntHyperDefinition HYPER_MAX_STEPS = new IntHyperDefinition("max-steps", 3, 200);
    public static IntHyperDefinition HYPER_NO_NODES = new IntHyperDefinition("no-nodes", 2, 100);

    public static void registerHyperparameter(HyperManager hyperManager) {
        hyperManager.get(HYPER_MAX_STEPS);
        hyperManager.get(HYPER_NO_NODES);
        MyValueDrawingHp.registerHyperparameter(hyperManager);
    }

    public static final String HYPER_SELECTION = """
            {
              'fraction_commands': 0.8867647226720414,
              'fraction_major_commands': 0.9989939340398684,
              'fraction_major_split': 0.924552028785329,
              'fraction_portals': 0.6575066779888346,
              'fraction_set': 0.9901816298961561,
              'max-steps': 15,
              'no-nodes': 53
            }
            """;

    public final ValueDrawing drawing;

    public static class Solution {
        public final ArithmeticForwardGame game;
        public final long netSeed;

        public Solution(ArithmeticForwardGame game, long netSeed) {
            this.game = game;
            this.netSeed = netSeed;
        }
    }

    static class MyValueDrawingHp extends ValueDrawingHp {
        public static DoubleHyperDefinition FRAC_PORTALS = new DoubleHyperDefinition("fraction_portals", 0.0, 1.0);
        public static void registerHyperparameter(HyperManager hyperManager) {
            hyperManager.get(FRAC_PORTALS);
            ValueDrawingHp.registerHyperparameter(hyperManager);
        }

        public MyValueDrawingHp(HyperManager hyperManager) {
            super(hyperManager);
        }

        @Override
        protected ValueDrawingHp.DistributionNode initializeDistribution() {
            double fracPortal = hyperManager.get(FRAC_PORTALS);
            ValueDrawingHp.DistributionNode distribution = super.initializeDistribution();
            return new ValueDrawingHp.DistributionSplit(fracPortal)
                    .first(new ValueDrawingHp.DistributionLeaf().add(new PortalValue(0, 1), 1.0))
                    .rest(distribution);
        }
    }

    private final int maxSteps;
    private final int numberOfNodes;
    private Integer numEpoch;

    private int numRuns;
    private final List<Solution> solutions = new ArrayList<>();
    private int nextSolutionIndex;

    private final UniformRandomProvider randMaster;
    private final UniformRandomProvider randNetSeeds;
    private final UniformRandomProvider randTargets;
    private int numGetOperation;
    private int numGetAnyOperand;
    private int numGetBothOperands;
    private int numGetAnyOperandBeforeEval;
    private int numGetBothOperandsBeforeEval;
    private int numPortalMoved;
    private int[] firstRepetitions;
    private int numNoRepetition;
    private Duration duration;

    private static class VerifierNetListener extends NopNetEventListener {

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
                hasEvalFlowed = true;
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

    public static void main(String[] args) throws IOException {
        if (WEB_UI) {
            WebUI.setUpJavalin();
        }
        if (true) {
            mainHpo();
        } else {
            StageReadInfo1 dev0 = null;//new StageReadInfo(30, 15, RandomUtil.newRandom2(12345L));
            dev0.runBatch();
        }
        if (WEB_UI) {
            WebUI.loopForever();
        }
    }

    public static void mainHpo() throws IOException {
        UniformRandomProvider randomRun = RandomUtil.newRandom2(12345L);

        CollectingHyperManager hyperCollector = new CollectingHyperManager();
        StageReadInfo1.registerHyperparameter(hyperCollector);

        RemoteHyperManager remoteHyperManager = new RemoteHyperManager(hyperCollector.getHyperDefinitions());
        remoteHyperManager.fixParameters(HYPER_SELECTION);

        for (int i = 0; i < 1; i++) {
            Integer trial = remoteHyperManager.suggest();

            StageReadInfo1 dev = new StageReadInfo1(remoteHyperManager, RandomUtil.newRandom2(randomRun.nextLong()));
            dev.setNumEpoch(500_000);

            dev.runBatch();

            int hits = dev.numPortalMoved;
            double hitsPerMinute = hits * 60_000.0 / dev.duration.toMillis();
            log.info("hpm: " + hitsPerMinute);

            remoteHyperManager.complete(trial, hitsPerMinute);
        }
    }

    public StageReadInfo1(HyperManager hyperManager, UniformRandomProvider random) {
        this.drawing = new MyValueDrawingHp(hyperManager);
        this.maxSteps = hyperManager.get(HYPER_MAX_STEPS);
        this.numberOfNodes = hyperManager.get(HYPER_NO_NODES);
        log.info("Suggestions, got maxSteps={}, numberOfNodes={}", maxSteps, numberOfNodes);
        this.randMaster = random;
        this.randNetSeeds = RandomUtil.newRandom2(randMaster.nextLong());
        this.randTargets = RandomUtil.newRandom2(randMaster.nextLong());
        if (TRACK_HASHES) {
            this.firstRepetitions = new int[maxSteps];
        }
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public void setNumEpoch(Integer numEpoch) {
        this.numEpoch = numEpoch;
    }

    public Solution getNextSolution() {
        while (solutions.size() <= nextSolutionIndex) {
            next();
        }
        Solution solution = solutions.get(nextSolutionIndex);
        nextSolutionIndex++;
        return solution;
    }

    private void runBatch() {
        long startTime = System.nanoTime();

        int numEpochReal = numEpoch != null ? numEpoch : 5_000_000;
        for (int i = 1; i <= numEpochReal; i++) {
            if (i % 20_000 == 0) {
                log.info("= i={} =", i);
                if (i % 100_000 == 0) {
                    printRunInfo(startTime);
                }
            }
            next();
        }
        this.duration = Duration.ofNanos(System.nanoTime() - startTime);
        printRunInfo(startTime);
    }

    private void printRunInfo(long startTime) {
        Duration duration = Duration.ofNanos(System.nanoTime() - startTime);
        log.info("total Runs: {}, get Operation: {}, get any Operand: {}, get both Operands: {}",
                numRuns,
                numGetOperation,
                numGetAnyOperand,
                numGetBothOperands);
        log.info("             get any operand before eval: {}", Tools.rate(numGetAnyOperandBeforeEval, numRuns));
        log.info("             get both operand before eval: {}", Tools.rate(numGetBothOperandsBeforeEval, numRuns));
        log.info("                portal moved: {}", Tools.rate(numPortalMoved, numRuns));
        log.info("                portal moved: {}", Tools.rate(numPortalMoved, numRuns));
        if (TRACK_HASHES) {
            log.info("  no repetition: {}", Tools.rate(numNoRepetition, numRuns));
            log.info("  first repetitions: {}", Arrays.toString(firstRepetitions));
        }
        log.info("TOTAL DURATION: {}", duration);
        log.info("Portal moved: {} hits / min", Tools.getHitsPerMinute(numPortalMoved, duration));
    }

    private void next() {
        int operand1 = ArithmeticForwardGame.drawOperand(randTargets);
        int operand2 = ArithmeticForwardGame.drawOperand(randTargets);
        TriOperation operation = ArithmeticForwardGame.drawOperation(randTargets);
        long netSeed = randNetSeeds.nextLong();

        ArithmeticForwardGame game = new ArithmeticForwardGame(operand1, operand2, operation, buildNet(netSeed), maxSteps);
        Holder<VerifierNetListener> holder = new Holder<>();
        game.setPostVerifierCreateCallback(verifier ->
                verifier.addEventListener(holder.put(new VerifierNetListener(game))));

        boolean interesting = false;// numRuns == 1000976;
        if (interesting) {
            game.setVerbose(true);
            class StageEntEventListener extends NopEntEventListener {
                @Override
                public void afterCommandExecution(StepResult stepResult) {
                    printRunInformation(game, holder);
                }
            }
            game.getEnt().addEventListener(new StageEntEventListener());
        }
        HashEntEventListener hashEntEventListener = null;
        if (TRACK_HASHES) {
            hashEntEventListener = new HashEntEventListener(game.getEnt());
            game.getEnt().addEventListener(hashEntEventListener);
        }

        game.execute();

        boolean isPortalMoved =  holder.get() != null && !holder.get().hasEvalFlowed && game.passedPortalMoved();
        if (isPortalMoved) {
            solutions.add(new Solution(game, netSeed));
        }

        recordRunInformation(game, holder);
        if (TRACK_HASHES) {
            if (hashEntEventListener.firstRepetition == null) {
                numNoRepetition++;
            } else {
                firstRepetitions[hashEntEventListener.firstRepetition]++;
            }
        }

        if (REPLAY_HITS) {
//            boolean isHit = holder.getListener() != null && holder.getListener().isAnyOperandBeforeEval();
//            boolean isHit =  holder.getListener() != null && !holder.getListener().hasEvalFlowed && game.passedPortalMoved();
            boolean isHit = TRACK_HASHES && hashEntEventListener.firstRepetition != null && hashEntEventListener.firstRepetition <= 3;
            if (isHit) {
                String uuid = UUID.randomUUID().toString();
                WebUiStoryOutput.addStory("game-"+uuid, () -> {
                    replayWithDetails(netSeed, operand1, operand2, operation);
                    log.info("replay done.");
                });
                WebUiStoryOutput.addStory("game-trimmed-"+uuid, () -> {
                    replayTrimmedWithDetails(netSeed, operand1, operand2, operation);
                    log.info("replay done.");
                });
                Logging.logHtml(() -> "hey <b>there</b>!<a href=\"/?story=game-%s\" target=\"_blank\">game</a> <a href=\"/?story=game-trimmed-%s\" target=\"_blank\">game-trimmed</a>".formatted(uuid, uuid));
                log.info("replay registered.");
            }
        }

        numRuns++;
        if (interesting) {
            System.err.println();
        }
    }

    private void replayWithDetails(long netSeed, int operand1, int operand2, TriOperation operation) {
        ArithmeticForwardGame game = new ArithmeticForwardGame(operand1, operand2, operation, buildNet(netSeed), maxSteps);
        replayWithDetails(game);
    }

    private void replayWithDetails(ArithmeticForwardGame game) {
        Holder<VerifierNetListener> holder = new Holder<>();
        game.setPostVerifierCreateCallback(verifier -> verifier.addEventListener(holder.put(new VerifierNetListener(game))));
        game.setVerbose(true);
        class StageEntEventListener extends NopEntEventListener {
            @Override
            public void afterCommandExecution(StepResult stepResult) {
                printRunInformation(game, holder);
            }
        }
        game.getEnt().addEventListener(new StageEntEventListener());

        game.execute();
        printRunInformation(game, holder);
    }

    private void replayTrimmedWithDetails(long netSeed, int operand1, int operand2, TriOperation operation) {
        ArithmeticForwardGame game0 = new ArithmeticForwardGame(operand1, operand2, operation, buildNet(netSeed), maxSteps);
        TrimmingListener trimmingListener = new TrimmingListener(game0.getEnt().getNet().getNodes().size());
        game0.getEnt().getNet().addEventListener(trimmingListener);
        game0.execute();

        ArithmeticForwardGame game = new ArithmeticForwardGame(operand1, operand2, operation, buildNet(netSeed), maxSteps);
        TrimmingHelper.trim(game.getEnt().getNet(), trimmingListener);
        replayWithDetails(game);
    }

    private void recordRunInformation(ArithmeticForwardGame game, Holder<VerifierNetListener> holder) {
        VerifierNetListener listener = holder.get();
        if (listener != null) {
            if (listener.numGetOperation > 0) {
                numGetOperation++;
            }
            if (listener.numGetOperand1 > 0 || listener.numGetOperand2 > 0) {
                numGetAnyOperand++;
                if (listener.numGetOperand1 > 0 && listener.numGetOperand2 > 0) {
                    numGetBothOperands++;
                }
            }
            if (listener.numGetOperand1BeforeEval > 0 || listener.numGetOperand2BeforeEval > 0) {
                numGetAnyOperandBeforeEval++;
                log.info("#{} Get Info Before Eval - x: {}, op: {}, y: {}", numRuns, listener.numGetOperand1BeforeEval, listener.numGetOperation, listener.numGetOperand2BeforeEval);
                if (listener.numGetOperand1BeforeEval > 0 && listener.numGetOperand2BeforeEval > 0) {
                    numGetBothOperandsBeforeEval++;
                }
            }
            if (game.passedPortalMoved() && !listener.hasEvalFlowed) {
//                log.info("#{} portal moved", numRuns);
                numPortalMoved++;
            }
        }
    }

    private void printRunInformation(ArithmeticForwardGame game, Holder<VerifierNetListener> holder) {
        VerifierNetListener listener = holder.get();
        if (listener != null) {
            if (listener.numGetOperand1BeforeEval > 0 || listener.numGetOperand2BeforeEval > 0) {
                log.info("#{} Get Info Before Eval - x: {}, op: {}, y: {}", numRuns, listener.numGetOperand1BeforeEval, listener.numGetOperation, listener.numGetOperand2BeforeEval);
            }
            if (game.passedPortalMoved() && !listener.hasEvalFlowed) {
                log.info("#{} portal moved", numRuns);
            }
        }

    }

    public Net buildNet(Long netSeed) {
        RandomNetCreator netCreator = new RandomNetCreator(numberOfNodes, RandomUtil.newRandom2(netSeed), drawing);
        return netCreator.drawNet();
    }
}
