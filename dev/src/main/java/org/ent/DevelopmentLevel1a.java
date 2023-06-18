package org.ent;

import org.ent.dev.randnet.RandomNetCreator;
import org.ent.dev.variation.ValueFragmentCrossover;
import org.ent.net.Net;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class DevelopmentLevel1a {
    private static final Logger log = LoggerFactory.getLogger(DevelopmentLevel1a.class);


    private final DevelopmentLevel0 developmentLevel0;

    private final Random randMaster;
    private final Random randTargetValue;

    public DevelopmentLevel1a() {
        this.developmentLevel0 = new DevelopmentLevel0();
        this.randMaster = new Random(0xe3f9c3);
        randMaster.nextInt();
        this.randTargetValue = new Random(randMaster.nextLong());
    }

    public static void main(String[] args) {
        DevelopmentLevel1a developmentLevel1A = new DevelopmentLevel1a();
        developmentLevel1A.next();
    }

    void investigate(long seed1, long seed2, long swapSeed, int targetValue) {
        RandomNetCreator netCreator1 = new RandomNetCreator(new Random(seed1), CopyValueGame.drawing);
        RandomNetCreator netCreator2 = new RandomNetCreator(new Random(seed2), CopyValueGame.drawing);
        Net net1 = netCreator1.drawNet();
        Net net2 = netCreator2.drawNet();

        new ValueFragmentCrossover(net1, net2, swapSeed).execute();

        CopyValueGame game1 = new CopyValueGame(targetValue, net1);
        game1.setVerbose(true);
        game1.execute();
    }

    int numPassing;

    boolean verbose = false;

    private void next() {
        long seed1 = developmentLevel0.nextGetTargetValue();
        long seed2 = developmentLevel0.nextInputSet();

        int numTotal = 0;
        int numMiss1 = 0;
        int numMiss2 = 0;
        int numHit1 = 0;
        int numHit2 = 0;
        int numFullHit1 = 0;
        int numFullHit2 = 0;

        for (int i = 1; i <= 10_000; i++) {
            int targetValue = randTargetValue.nextInt(5, 17);
            if (i % 50 == 0) {
                do {
                    seed1 = developmentLevel0.nextGetTargetValue();
                } while (isPassing(seed1, targetValue));
                do {
                    seed2 = developmentLevel0.nextInputSet();
                } while (isPassing(seed2, targetValue));
            }
            RandomNetCreator netCreator1 = new RandomNetCreator(new Random(seed1), CopyValueGame.drawing);
            RandomNetCreator netCreator2 = new RandomNetCreator(new Random(seed2), CopyValueGame.drawing);
            Net net1 = netCreator1.drawNet();
            Net net2 = netCreator2.drawNet();

            long swapSeed = randMaster.nextLong();
            ValueFragmentCrossover crossover = new ValueFragmentCrossover(net1, net2, swapSeed);
            crossover.execute();

            CopyValueGame game1 = new CopyValueGame(targetValue, net1);
            game1.execute();

            if (verbose) {
                log.info("result1: {} {}",
                        game1.passedGetTargetValue() ? "t" : "target_value missed",
                        game1.passedInputSet() ? "does INPUT_SET!" : "");
            }
            if (!game1.passedGetTargetValue()) {
                numMiss1++;
            }
            if (game1.passedInputSet()) {
                numHit1++;
            }
            if (game1.passedInputSetToTargetValue()) {
                numFullHit1++;
                log.info("  GOAL reached: input set to target value (1) 0x{}L, 0x{}L, 0x{}L, {}", Long.toHexString(seed1), Long.toHexString(seed2), Long.toHexString(swapSeed), targetValue);
            }

            CopyValueGame game2 = new CopyValueGame(targetValue, net2);


            game2.execute();

            if (verbose) {
                log.info("result2: {} {}",
                        game2.passedInputSet() ? "i" : "input_set missed",
                        game2.passedGetTargetValue() ? "does TARGET_VALE!" : "");
            }
            if (!game2.passedInputSet()) {
                numMiss2++;
            }
            if (game2.passedGetTargetValue()) {
                numHit2++;
            }
            if (game2.passedInputSetToTargetValue()) {
                numFullHit2++;
                log.info("  GOAL reached: input set to target value (2) 0x{}L, 0x{}L, 0x{}L, {}", Long.toHexString(seed1), Long.toHexString(seed2), Long.toHexString(swapSeed), targetValue);
            }
            numTotal++;
        }
        log.info("");
        log.info("miss 1: {}, miss 2: {}", rate(numMiss1, numTotal), rate(numMiss2, numTotal));
        log.info("hit 1: {}, hit 2: {}", rate(numHit1, numTotal), rate(numHit2, numTotal));
        log.info("num passing: {}", numPassing);
        if (numFullHit1 + numFullHit2 > 0) {
            log.info("full hit 1: {}, full hit 2: {}", rate(numFullHit1, numTotal), rate(numFullHit2, numTotal));
        } else {
            log.info("no full hit");
        }
    }

    private boolean isPassing(long seed, int targetValue) {
        RandomNetCreator netCreator = new RandomNetCreator(new Random(seed), CopyValueGame.drawing);
        Net net = netCreator.drawNet();
        CopyValueGame game = new CopyValueGame(targetValue, net);
        game.execute();
        boolean passing = game.passedInputSetToTargetValue();
        if (passing) {
            log.info("was passing {} on {}", Long.toHexString(seed), targetValue);
            numPassing++;
        }
        return passing;
    }

    private String rate(int count, int total) {
        return "%d / %d (%.2f %%)".formatted(count, total, ((double) count) / total * 100);
    }

}
