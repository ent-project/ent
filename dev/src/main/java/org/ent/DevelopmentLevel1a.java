package org.ent;

import org.ent.dev.randnet.RandomNetCreator;
import org.ent.dev.variation.ValueFragmentCrossover;
import org.ent.net.Net;
import org.ent.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class DevelopmentLevel1a {
    private static final Logger log = LoggerFactory.getLogger(DevelopmentLevel1a.class);
    public static final int ATTEMPTS_PER_UPSTREAM = 400;
    private final double CROSSOVER_FREQUENCY_FACTOR = 1.0;
    private int maxStepsLevel0 = 6;
    private int maxSteps = 8;
    private long masterSeed = 0xfa11afel;


    private final DevelopmentLevel0 developmentLevel0;

    private final Random randMaster;
    private final Random randTargetValue;

    boolean verbose = false;

    Long seed1, seed2;
    private final List<CopyValueGame> goodSeeds = new ArrayList<>();
    private int nextIndexGoodSeeds;

    private final Stat stat1 = new Stat(1, CopyValueGame::passedGetTargetValue);
    private final Stat stat2 = new Stat(2, CopyValueGame::passedInputSet);
    private int numUpstream1DirectHit, numUpstream2DirectHit;
    private int numUpstream1Total, numUpstream2Total;

    static class Stat {
        private final int id;
        private final Predicate<CopyValueGame> retainTest;
        private int numTotal;
        private int numRetained;
        private int numDegraded;
        private int numHit;

        Stat(int id, Predicate<CopyValueGame> retainTest) {
            this.id = id;
            this.retainTest = retainTest;
        }
    }

    public DevelopmentLevel1a() {
        this.randMaster = new Random(masterSeed);
        this.developmentLevel0 = new DevelopmentLevel0(maxStepsLevel0, new Random(randMaster.nextLong()));
        this.randTargetValue = new Random(randMaster.nextLong());
    }

    public static void main(String[] args) {
        DevelopmentLevel1a dev = new DevelopmentLevel1a();
        dev.run();
    }

    void investigate(long seed1, long seed2, long swapSeed, int targetValue) {
        RandomNetCreator netCreator1 = new RandomNetCreator(new Random(seed1), CopyValueGame.drawing);
        RandomNetCreator netCreator2 = new RandomNetCreator(new Random(seed2), CopyValueGame.drawing);
        Net net1 = netCreator1.drawNet();
        Net net2 = netCreator2.drawNet();

        new ValueFragmentCrossover(net1, net2, swapSeed, CROSSOVER_FREQUENCY_FACTOR).execute();

        CopyValueGame game1 = new CopyValueGame(targetValue, net1, maxSteps);
        game1.setVerbose(true);
        game1.execute();
    }

    private void run() {
        long startTime = System.nanoTime();

//        investigate(0x84bde80f4f9f8c6aL, 0xfe4e51e929f262b2L, 0x923080f3bcf65cb7L, 6);

        for (int i = 0; i < 400; i++) {
            if (i % 20 == 0) {
                log.info("## i = {}", i);
            }
            getNextInputSetToTargetValue();
        }

        Duration duration = Duration.ofNanos(System.nanoTime() - startTime);
        log.info("");
        log.info("crossover frequency factor: {}, attempts per upstream: {}, max steps lvl0/lvl1a: {}/{} ", CROSSOVER_FREQUENCY_FACTOR, ATTEMPTS_PER_UPSTREAM, maxStepsLevel0, maxSteps);
        log.info("miss 1: {}, miss 2: {}", rate(stat1.numDegraded, stat1.numTotal), rate(stat2.numDegraded, stat2.numTotal));
        log.info("retained 1: {}, retained 2: {}", rate(stat1.numRetained, stat1.numTotal), rate(stat2.numRetained, stat2.numTotal));
        log.info("upstream direct hit: {} + {} = {}",
                rate(numUpstream1DirectHit, numUpstream1Total),
                rate(numUpstream2DirectHit, numUpstream2Total),
                rate(numUpstream1DirectHit + numUpstream2DirectHit, numUpstream1Total + numUpstream2Total));
        if (stat1.numHit + stat2.numHit > 0) {
            log.info("hit 1: {}, hit 2: {}", rate(stat1.numHit, stat1.numTotal), rate(stat2.numHit, stat2.numTotal));
        } else {
            log.info("no hit");
        }
        log.info("TOTAL DURATION: {}", duration);
        log.info("excluding direct hits: {} hits / min", Tools.getHitsPerMinute(stat1.numHit + stat2.numHit, duration));
        log.info("including direct hits: {} hits / min", Tools.getHitsPerMinute(stat1.numHit + stat2.numHit + numUpstream1DirectHit + numUpstream2DirectHit, duration));
    }

    public CopyValueGame getNextInputSetToTargetValue() {
        while (goodSeeds.size() <= nextIndexGoodSeeds) {
            next();
        }
        CopyValueGame result = goodSeeds.get(nextIndexGoodSeeds);
        nextIndexGoodSeeds++;
        return result;
    }

    private void next() {
        int targetValue = randTargetValue.nextInt(5, 17);
        if (seed1 == null) {
            CopyValueGame upstream1 = developmentLevel0.nextGetTargetValue();
            numUpstream1Total++;
            if (upstream1.passedInputSetToTargetValue()) {
                numUpstream1DirectHit++;
                goodSeeds.add(upstream1);
            } else {
                seed1 = upstream1.getNetCreatorSeed();
            }
        }
        if (seed2 == null) {
            CopyValueGame upstream2 = developmentLevel0.nextInputSet();
            numUpstream2Total++;
            if (upstream2.passedInputSetToTargetValue()) {
                numUpstream2DirectHit++;
                goodSeeds.add(upstream2);
            } else {
                seed2 = upstream2.getNetCreatorSeed();
            }
        }
        if (seed1 == null || seed2 == null) {
            return;
        }

        int found1 = 0, found2 = 0;
        for (int i = 0; i < ATTEMPTS_PER_UPSTREAM; i++) {
            RandomNetCreator netCreator1 = new RandomNetCreator(new Random(seed1), CopyValueGame.drawing);
            RandomNetCreator netCreator2 = new RandomNetCreator(new Random(seed2), CopyValueGame.drawing);
            Net net1 = netCreator1.drawNet();
            Net net2 = netCreator2.drawNet();

            long swapSeed = randMaster.nextLong();
            new ValueFragmentCrossover(net1, net2, swapSeed, CROSSOVER_FREQUENCY_FACTOR).execute();

            if (found1 < 2) {
                CopyValueGame game1 = new CopyValueGame(targetValue, net1, maxSteps);
                game1.execute();
                if (recordSuccess(game1, stat1, swapSeed)) {
                    goodSeeds.add(game1);
                    found1++;
                }
            }

            if (found2 < 2) {
                CopyValueGame game2 = new CopyValueGame(targetValue, net2, maxSteps);
                game2.execute();
                if (recordSuccess(game2, stat2, swapSeed)) {
                    goodSeeds.add(game2);
                    found2++;
                }
            }

            if (found1 > 0 && found2 > 0) {
                break;
            }
        }

        seed1 = null;
        seed2 = null;
    }

    private boolean recordSuccess(CopyValueGame game, Stat stat, long swapSeed) {
//        if (verbose) {
//            log.info("result{}: {} {}",
//                    stat.id,
//                    game.passedGetTargetValue() ? "t" : "target_value missed",
//                    game.passedInputSet() ? "does INPUT_SET!" : "");
//        }
        stat.numTotal++;
        if (stat.retainTest.test(game)) {
            stat.numRetained++;
        } else {
            stat.numDegraded++;
        }
        if (game.passedInputSetToTargetValue()) {
            stat.numHit++;
            log.info("  GOAL reached: input set to target value ({}) 0x{}L, 0x{}L, 0x{}L, {}",
                    stat.id,
                    Long.toHexString(seed1),
                    Long.toHexString(seed2),
                    Long.toHexString(swapSeed),
                    game.getTargetValue());
            return true;
        }
        return false;
    }

    private String rate(int count, int total) {
        return "%d / %d (%.2f %%)".formatted(count, total, ((double) count) / total * 100);
    }
}
