package org.ent.dev.game.forwardarithmetic;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.NopEntEventListener;
import org.ent.NopNetEventListener;
import org.ent.dev.randnet.PortalValue;
import org.ent.dev.randnet.RandomNetCreator;
import org.ent.dev.randnet.ValueDrawing;
import org.ent.dev.randnet.ValueDrawingHp;
import org.ent.hyper.CollectingHyperManager;
import org.ent.hyper.HyperManager;
import org.ent.hyper.RemoteHyperManager;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.operation.TriOperation;
import org.ent.net.util.RandomUtil;
import org.ent.run.StepResult;
import org.ent.util.Tools;
import org.ent.webui.WebUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;

public class StageReadInfo {

    private static final boolean WEB_UI = false;
    public static final boolean REPLAY_HITS = false || WEB_UI;

    private static final Logger log = LoggerFactory.getLogger(StageReadInfo.class);

    public final ValueDrawing drawing;

    static class MyValueDrawingHp extends ValueDrawingHp {
        public MyValueDrawingHp(HyperManager hyperManager) {
            super(hyperManager);
        }

        @Override
        protected ValueDrawingHp.DistributionNode initializeDistribution() {
            double fracPortal = hyperManager.getDouble("fraction_portals", 0.0f, 1.0f);
            ValueDrawingHp.DistributionNode distribution = super.initializeDistribution();
            if (hyperManager.isCollecting()) {
                return null;
            }
            return new ValueDrawingHp.DistributionSplit(fracPortal)
                    .first(new ValueDrawingHp.DistributionLeaf().add(new PortalValue(0, 1), 1.0))
                    .rest(distribution);
        }
    }

    private final int maxSteps;
    private final int numberOfNodes;
    private Integer numEpoch;

    private int numRuns;

    private final UniformRandomProvider randMaster;
    private final UniformRandomProvider randNetSeeds;
    private final UniformRandomProvider randTargets;
    private int numGetOperation;
    private int numGetAnyOperand;
    private int numGetBothOperands;
    private int numGetAnyOperandBeforeEval;
    private int numGetBothOperandsBeforeEval;
    private int numPortalMoved;
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

    private static class ListenerHolder {
        VerifierNetListener listener;

        VerifierNetListener create(ArithmeticForwardGame game) {
            listener = new VerifierNetListener(game);
            return listener;
        }

        public VerifierNetListener getListener() {
            return listener;
        }
    }

    public static void main(String[] args) throws IOException {
        if (WEB_UI) {
            WebUI.setUpJavalin();
        }
        if (true) {
            mainHpo();
        } else {
            StageReadInfo dev0 = null;//new StageReadInfo(30, 15, RandomUtil.newRandom2(12345L));
            dev0.run();
        }
        if (WEB_UI) {
            WebUI.loopForever();
        }
    }

    public static void mainHpo() throws IOException {
        UniformRandomProvider randomRun = RandomUtil.newRandom2(12345L);

        CollectingHyperManager hyperCollector = new CollectingHyperManager();
        new StageReadInfo(hyperCollector, RandomUtil.newRandom2(5L));

        RemoteHyperManager remoteHyperManager = new RemoteHyperManager(hyperCollector.getHyperDefinitions());

        for (int i = 0; i < 200000000; i++) {
            int trial = remoteHyperManager.suggest();

            StageReadInfo dev = new StageReadInfo(remoteHyperManager, RandomUtil.newRandom2(randomRun.nextLong()));
            dev.setNumEpoch(500_000);

            dev.run();

            int hits = dev.numPortalMoved;
            double hitsPerMinute = hits * 60_000.0 / dev.duration.toMillis();
            log.info("hpm: " + hitsPerMinute);

            remoteHyperManager.complete(trial, hitsPerMinute);
        }
    }

    public StageReadInfo(HyperManager hyperManager, UniformRandomProvider random) {
        this.drawing = new MyValueDrawingHp(hyperManager);
        this.maxSteps = hyperManager.getInt("max-steps", 3, 200);
        this.numberOfNodes = hyperManager.getInt("no-nodes", 2, 100);
        log.info("Suggestions, got maxSteps={}, numberOfNodes={}", maxSteps, numberOfNodes);
        this.randMaster = random;
        this.randNetSeeds = RandomUtil.newRandom2(randMaster.nextLong());
        this.randTargets = RandomUtil.newRandom2(randMaster.nextLong());
    }

    public void setNumEpoch(Integer numEpoch) {
        this.numEpoch = numEpoch;
    }

    private void run() {
        long startTime = System.nanoTime();

        int numEpochReal = numEpoch != null ? numEpoch : 5_000_000;
        for (int i = 0; i < numEpochReal; i++) {
            if (i % 20_000 == 0) {
                log.info("= i={} =", i);
                if (i % 100_000 == 0) {
                    printRunInfo(startTime);
                }
            }
            performRun();
        }
        this.duration = Duration.ofNanos(System.nanoTime() - startTime);
        printRunInfo(startTime);
    }

    private void printRunInfo(long startTime) {
        Duration duration = Duration.ofNanos(System.nanoTime() - startTime);
        log.info("total Runs: {}, get any Operand before Eval: {}, get both operands before Eval:{}, get Operation: {}, get any Operand: {}, get both Operands: {}",
                numRuns,
                numGetAnyOperandBeforeEval,
                numGetBothOperandsBeforeEval,
                numGetOperation,
                numGetAnyOperand,
                numGetBothOperands);
        log.info("                portal moved: {}", numPortalMoved);
        log.info("TOTAL DURATION: {}", duration);
        log.info("Portal moved: {} hits / min", Tools.getHitsPerMinute(numPortalMoved, duration));
    }

    private void performRun() {
        int operand1 = ArithmeticForwardGame.drawOperand(randTargets);
        int operand2 = ArithmeticForwardGame.drawOperand(randTargets);
        TriOperation operation = ArithmeticForwardGame.drawOperation(randTargets);
        long netSeed = randNetSeeds.nextLong();
        ArithmeticForwardGame game = new ArithmeticForwardGame(operand1, operand2, operation, buildNet(netSeed), maxSteps);
        ListenerHolder holder = new ListenerHolder();
        game.setPostVerifierCreateCallback(verifier -> verifier.addEventListener(holder.create(game)));
//        if (numRuns == 633937 ){//|| numRuns ==1000976) { //1184310
        boolean interesting = false;// numRuns == 1000976;
//        boolean interesting = numRuns == 1;
        if (interesting) {//|| numRuns ==1000976) { //1184310
            game.setVerbose(true);
            class StageEntEventListener extends NopEntEventListener {
                @Override
                public void afterCommandExecution(StepResult stepResult) {
                    printRunInformation(game, holder);
                }
            }
            game.getEnt().setEventListener(new StageEntEventListener());
        }

        game.execute();

        recordRunInformation(game, holder);

        if (REPLAY_HITS) {
//            boolean isHit = holder.getListener() != null && holder.getListener().isAnyOperandBeforeEval();
            boolean isHit =  holder.getListener() != null && !holder.getListener().hasEvalFlowed && game.passedPortalMoved();
            if (isHit) {
                replayWithDetails(netSeed, operand1, operand2, operation);
                log.info("replay done.");
            }
        }

        numRuns++;
        if (interesting) {
            System.err.println();
        }
    }

    private void replayWithDetails(long netSeed, int operand1, int operand2, TriOperation operation) {
        ArithmeticForwardGame game = new ArithmeticForwardGame(operand1, operand2, operation, buildNet(netSeed), maxSteps);
        ListenerHolder holder = new ListenerHolder();
        game.setPostVerifierCreateCallback(verifier -> verifier.addEventListener(holder.create(game)));
        game.setVerbose(true);
        class StageEntEventListener extends NopEntEventListener {
            @Override
            public void afterCommandExecution(StepResult stepResult) {
                printRunInformation(game, holder);
            }
        }
        game.getEnt().setEventListener(new StageEntEventListener());

        game.execute();
        printRunInformation(game, holder);
    }

    private void recordRunInformation(ArithmeticForwardGame game, ListenerHolder holder) {
        VerifierNetListener listener = holder.getListener();
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

    private void printRunInformation(ArithmeticForwardGame game, ListenerHolder holder) {
        VerifierNetListener listener = holder.getListener();
        if (listener != null) {
            if (listener.numGetOperand1BeforeEval > 0 || listener.numGetOperand2BeforeEval > 0) {
                log.info("#{} Get Info Before Eval - x: {}, op: {}, y: {}", numRuns, listener.numGetOperand1BeforeEval, listener.numGetOperation, listener.numGetOperand2BeforeEval);
            }
            if (game.passedPortalMoved() && !listener.hasEvalFlowed) {
                log.info("#{} portal moved", numRuns);
            }
        }

    }


    private Net buildNet(Long netSeed) {
        RandomNetCreator netCreator = new RandomNetCreator(numberOfNodes, RandomUtil.newRandom2(netSeed), drawing);
        return netCreator.drawNet();
    }

}
