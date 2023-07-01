package org.ent;

import org.ent.dev.randnet.DefaultValueDrawing;
import org.ent.dev.randnet.PortalValue;
import org.ent.dev.randnet.RandomNetCreator;
import org.ent.net.Net;
import org.ent.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DevelopmentLevel0 {

    private static final Logger log = LoggerFactory.getLogger(DevelopmentLevel0.class);

    private final int maxSteps;
    private final int numberOfNodes;

    private int numRuns;

    private final Random randMaster;
    private final Random randNetSeeds;
    private final Random randTargets;

    private final DefaultValueDrawing drawing;

    private final List<CopyValueGame> goodSeedsGetTargetValue = new ArrayList<>();
    private final List<CopyValueGame> goodSeedsInputSet = new ArrayList<>();
    private final List<CopyValueGame> goodSeedsEvalFlowOnVerifierRoot = new ArrayList<>();
    private int lastIndexGetTargetValue = -1;
    private int lastIndexInputSet = -1;
    private int lastIndexEvalFlowOnVerifierRoot = -1;

    public static void main(String[] args) {
        DevelopmentLevel0 dev0 = new DevelopmentLevel0(100, 15, new Random(5L));
        dev0.run();
    }

    public void run() {
        long startTime = System.nanoTime();
        int numHits = 0;
        int numFullHits = 0;
//        dev0.investigate(0xdfec9244c7cf21cdL, 5);
        for (int i = 0; i < 1000; i++) {
            CopyValueGame game = nextEvalFlowOnVerifierRoot();
            log.info("seed: {}", Long.toHexString(game.getNetCreatorSeed()));
//            log.info("gtv: {} is: {}", dev0.goodSeedsGetTargetValue.size(), dev0.goodSeedsInputSet.size());
            if (game.passedVerifierFinished()) {
                numFullHits++;
            }
            numHits++;
        }
        Duration duration = Duration.ofNanos(System.nanoTime() - startTime);
        log.info("total runs: {}; getTargetValue: {}; inputSet: {}; evalFlow: {}",
                numRuns,
                goodSeedsGetTargetValue.size(),
                goodSeedsInputSet.size(),
                goodSeedsEvalFlowOnVerifierRoot.size());
        log.info("TOTAL DURATION: {}", duration);
        log.info("eval-flow on verifier: {} hits / min", Tools.getHitsPerMinute(numHits, duration));
        log.info("verifier finished: {} hits / min", Tools.getHitsPerMinute(numFullHits, duration));
    }

    private void investigate(long seed, int targetValue) {
        RandomNetCreator netCreator = new RandomNetCreator(numberOfNodes, new Random(seed), CopyValueGame.drawing);
        Net net = netCreator.drawNet();
        CopyValueGame game = new CopyValueGame(targetValue, net, maxSteps);
        game.setVerbose(true);
        game.execute();
    }

    public DevelopmentLevel0(int maxSteps, int numberOfNodes, Random rand) {
        this.maxSteps = maxSteps;
        this.numberOfNodes = numberOfNodes;
        randMaster = rand;
        randNetSeeds = new Random(randMaster.nextLong());
        randTargets = new Random(randMaster.nextLong());

        drawing = new DefaultValueDrawing();
        drawing.addValueBase(new PortalValue(0, 1), DefaultValueDrawing.WEIGHT3);
    }

    public CopyValueGame getNextGetTargetValue() {
        lastIndexGetTargetValue++;
        while (goodSeedsGetTargetValue.size() <= lastIndexGetTargetValue) {
            performRun();
        }
        return goodSeedsGetTargetValue.get(lastIndexGetTargetValue);
    }

    public CopyValueGame nextInputSet() {
        lastIndexInputSet++;
        while (goodSeedsInputSet.size() <= lastIndexInputSet) {
            performRun();
        }
        return goodSeedsInputSet.get(lastIndexInputSet);
    }

    public CopyValueGame nextEvalFlowOnVerifierRoot() {
        lastIndexEvalFlowOnVerifierRoot++;
        while (goodSeedsEvalFlowOnVerifierRoot.size() <= lastIndexEvalFlowOnVerifierRoot) {
            performRun();
        }
        return goodSeedsEvalFlowOnVerifierRoot.get(lastIndexEvalFlowOnVerifierRoot);
    }

    private void performRun() {
        int targetValue = randTargets.nextInt(5, 12);
        long netCreatorSeed = randNetSeeds.nextLong();

        CopyValueGame game = new CopyValueGame(targetValue, netCreatorSeed, maxSteps, numberOfNodes);
        game.execute();
        numRuns++;
//        if (game.passedEvalFlowOnVerifierRoot() || game.passedVerifierFinished()) {
//            log.info("==================== y={} seed={}", targetValue, Long.toHexString(netCreatorSeed));
//            game.dumpResults();
//        }
        if (game.passedGetTargetValue()) {
            goodSeedsGetTargetValue.add(game);
        }
        if (game.passedInputSet()) {
            goodSeedsInputSet.add(game);
        }
        if (game.passedEvalFlowOnVerifierRoot()) {
            goodSeedsEvalFlowOnVerifierRoot.add(game);
        }
    }

}
