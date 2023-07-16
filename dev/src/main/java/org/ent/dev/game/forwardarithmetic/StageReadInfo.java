package org.ent.dev.game.forwardarithmetic;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.rng.UniformRandomProvider;
import org.ent.NopEntEventListener;
import org.ent.NopNetEventListener;
import org.ent.dev.randnet.DefaultValueDrawing;
import org.ent.dev.randnet.PortalValue;
import org.ent.dev.randnet.RandomNetCreator;
import org.ent.hyper.HpoService;
import org.ent.hyper.HyperDefinition;
import org.ent.hyper.IntHyperDefinition;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.net.node.cmd.operation.TriOperation;
import org.ent.net.util.RandomUtil;
import org.ent.run.StepResult;
import org.ent.util.Tools;
import org.ent.webui.WebUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class StageReadInfo {

    private static final boolean WEB_UI = false;

    private static final Logger log = LoggerFactory.getLogger(StageReadInfo.class);

    public static final DefaultValueDrawing drawing = new MyValueDrawing();

    static class MyValueDrawing extends DefaultValueDrawing {
        @Override
        protected void initializeValues() {
            addValueBase(Operations.SET_OPERATION, WEIGHT1);
            addValueBase(Operations.SET_VALUE_OPERATION, WEIGHT1);
            addValueBase(new PortalValue(0, 1), DefaultValueDrawing.WEIGHT3);
        }
    }
//    static {
//        drawing = new DefaultValueDrawing();
//        drawing.addValueBase(new PortalValue(0, 1), DefaultValueDrawing.WEIGHT3);
//    }

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
    private Duration duration;

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

    public static void main(String[] args) throws IOException {
        if (false) {
            for (int i = 0; i < 200; i++) {
                mainHpo();
            }
        } else {
            if (WEB_UI) {
                WebUI.setUpJavalin();
            }
            StageReadInfo dev0 = new StageReadInfo(30, 15, RandomUtil.newRandom2(12345L));
            dev0.run();
            if (WEB_UI) {
                WebUI.loopForever();
            }
        }
    }

    public static void mainHpo() throws IOException {
        UniformRandomProvider randomRun = RandomUtil.newRandom2(12345L);
        ObjectMapper objectMapper = new ObjectMapper();
        OkHttpClient okHttpClient = new OkHttpClient();

        List<HyperDefinition> hyperDefinitions = List.of(
                new IntHyperDefinition("maxSteps", 3, 200),
                new IntHyperDefinition("noNodes", 2, 100));

        String jsonInputString = objectMapper.writeValueAsString(hyperDefinitions);
        RequestBody requestBody = RequestBody.create(
                jsonInputString,
                MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url("http://localhost:5005/suggest")
                .post(requestBody)
                .build();

        Response response = okHttpClient.newCall(request).execute();
        String responseBody = response.body().string();
        System.err.println(responseBody);
        HpoService.SuggestResponse suggestResponse = objectMapper.readValue(responseBody, HpoService.SuggestResponse.class);

        Map<String, Object> hps = suggestResponse.getParameters();
        log.info("Hyperparameters: " + hps);
        int maxStepsHP = (int) (hps.get("maxSteps"));
        int numberOfNodesHP = (int) (hps.get("noNodes"));
        StageReadInfo dev = new StageReadInfo(maxStepsHP, numberOfNodesHP, RandomUtil.newRandom2(randomRun.nextLong()));
        dev.setNumEpoch(50_000);

        dev.run();

        int hits = dev.numGetAnyOperand;
        double hitsPerMinute = hits * 60_000.0 / dev.duration.toMillis();
        log.info("hpm: " + hitsPerMinute);

        complete(suggestResponse.getTrial_number(), hitsPerMinute, okHttpClient);
    }

    private static void complete(int trialNumber, double value, OkHttpClient okHttpClient) throws IOException {
        HpoService.CompleteRequest completeRequest = new HpoService.CompleteRequest(trialNumber, value);
        ObjectMapper objectMapper = new ObjectMapper();
        String completeRequestJson = objectMapper.writeValueAsString(completeRequest);

        RequestBody requestBody = RequestBody.create(
                completeRequestJson,
                MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url("http://localhost:5005/complete")
                .post(requestBody)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            System.err.println("complete response: " + response);
        }
    }

    public StageReadInfo(int maxSteps, int numberOfNodes, UniformRandomProvider random) {
        this.maxSteps = maxSteps;
        this.numberOfNodes = numberOfNodes;
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
        log.info("TOTAL DURATION: {}", duration);
        log.info("Get any Operand before eval: {} hits / min", Tools.getHitsPerMinute(numGetAnyOperandBeforeEval, duration));
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
                    printRunInformation(holder);
                }
            }
            game.getEnt().setEventListener(new StageEntEventListener());
        }

        game.execute();

        printRunInformation(holder);

        boolean replayHits = false;
        if (replayHits) {
            if (holder.getListener() != null && holder.getListener().isAnyOperandBeforeEval()) {
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
