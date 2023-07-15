package org.ent.dev.game.forwardarithmetic;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.NopEntEventListener;
import org.ent.NopNetEventListener;
import org.ent.dev.randnet.DefaultValueDrawing;
import org.ent.dev.randnet.PortalValue;
import org.ent.dev.randnet.RandomNetCreator;
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

import java.time.Duration;

public class StageReadInfo {

    private static final boolean WEB_UI = true;

    private static final Logger log = LoggerFactory.getLogger(StageReadInfo.class);

    public static final DefaultValueDrawing drawing;

    static {
        drawing = new DefaultValueDrawing();
        drawing.addValueBase(new PortalValue(0, 1), DefaultValueDrawing.WEIGHT3);
    }

    private final int maxSteps;
    private final int numberOfNodes;

    private int numRuns;

    private final UniformRandomProvider randMaster;
    private final UniformRandomProvider randNetSeeds;
    private final UniformRandomProvider randTargets;
    private int numGetOperation;
    private int numGetAnyOperand;
    private int numGetBothOperands;
    private int numGetAnyOperandBeforeEval;
    private int numGetBothOperandsBeforeEval;

    private static class VerifierNetListener extends NopNetEventListener {

        private final ArithmeticForwardGame game;

        private int numGetValue;
        private int numGetOperation;
        private int numGetOperand1;
        private int numGetOperand2;
        private boolean hasEvaluatedOperation;
        private int numGetOperand1BeforeEval;
        private int numGetOperand2BeforeEval;

        private VerifierNetListener(ArithmeticForwardGame game) {
            this.game = game;
        }

        @Override
        public void beforeEvalExecution(Node target, boolean flow) {
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

    public static void main(String[] args) {
        if (WEB_UI) {
            WebUI.setUpJavalin();
        }
        StageReadInfo dev0 = new StageReadInfo(30, 15, RandomUtil.newRandom2(12345L));
        dev0.run();
        if (WEB_UI) {
            WebUI.loopForever();
        }
    }

    public StageReadInfo(int maxSteps, int numberOfNodes, UniformRandomProvider random) {
        this.maxSteps = maxSteps;
        this.numberOfNodes = numberOfNodes;
        this.randMaster = random;
        this.randNetSeeds = RandomUtil.newRandom2(randMaster.nextLong());
        this.randTargets = RandomUtil.newRandom2(randMaster.nextLong());
    }

    private void run() {
        long startTime = System.nanoTime();

        for (int i = 0; i < 5000_000; i++) {
            if (i % 20_000 == 0) {
                log.info("= i={} =", i);
                if (i % 100_000 == 0) {
                    printRunInfo(startTime);
                }
            }
            performRun();
        }
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
        log.info("TOTAL DURATION: {}", duration);
        log.info("Get any Operand: {} hits / min", Tools.getHitsPerMinute(numGetAnyOperandBeforeEval, duration));
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
        if (interesting){//|| numRuns ==1000976) { //1184310
            game.setVerbose(true);
            class StageEntEventListener extends NopEntEventListener {
                @Override
                public void afterCommandExecution(StepResult stepResult) {
                    printRunInformation(holder);
                }
            }
            game.getEnt().setEventListener(new StageEntEventListener());
        }

        game.execute();

        printRunInformation(holder);

        if (holder.getListener() != null && holder.getListener().isAnyOperandBeforeEval()) {
            replayWithDetails(netSeed, operand1, operand2, operation);
            log.info("replay done.");
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
                printRunInformation(holder);
            }
        }
        game.getEnt().setEventListener(new StageEntEventListener());

        game.execute();
        printRunInformation(holder);
    }

    private void printRunInformation(ListenerHolder holder) {
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
        }
    }

    private Net buildNet(Long netSeed) {
        RandomNetCreator netCreator = new RandomNetCreator(numberOfNodes, RandomUtil.newRandom2(netSeed), drawing);
        return netCreator.drawNet();
    }

}
