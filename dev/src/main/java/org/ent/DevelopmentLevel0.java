package org.ent;

import org.ent.dev.randnet.DefaultValueDrawing;
import org.ent.dev.randnet.PortalValue;
import org.ent.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DevelopmentLevel0 {

    private static final Logger log = LoggerFactory.getLogger(DevelopmentLevel0.class);

    public static final int MAX_STEPS = 100; // hyper-parameter

    public static void main(String[] args) {
        DevelopmentLevel0 dev0 = new DevelopmentLevel0();
        long startTime = System.nanoTime();
        int numHits = 0;
        for (int i = 0; i < 1000; i++) {
            long seed = dev0.nextGetTargetValue();
//            log.info("seed: {}", Long.toHexString(seed));
//            log.info("gtv: {} is: {}", dev0.goodSeedsGetTargetValue.size(), dev0.goodSeedsInputSet.size());
            numHits++;
        }
        Duration duration = Duration.ofNanos(System.nanoTime() - startTime);
        log.info("TOTAL DURATION: {}\n{} hits / min", duration, Tools.getHitsPerMinute(numHits, duration));
    }


    private final Random randMaster;
    private final Random randNetSeeds;
    private final Random randTargets;

    private final DefaultValueDrawing drawing;

    private final List<Long> goodSeedsGetTargetValue = new ArrayList<>();
    private final List<Long> goodSeedsInputSet = new ArrayList<>();
    private int lastIndexGetTargetValue = -1;
    private int lastIndexInputSet = -1;

    public DevelopmentLevel0() {
        randMaster = new Random(0x17abc);
        randNetSeeds = new Random(randMaster.nextLong());
        randTargets = new Random(randMaster.nextLong());

        drawing = new DefaultValueDrawing();
        drawing.addValueBase(new PortalValue(0, 1), DefaultValueDrawing.WEIGHT3);
    }

    public long nextGetTargetValue() {
        lastIndexGetTargetValue++;
        while (goodSeedsGetTargetValue.size() <= lastIndexGetTargetValue) {
            performRun();
        }
        return goodSeedsGetTargetValue.get(lastIndexGetTargetValue);
    }

    public long nextInputSet() {
        lastIndexInputSet++;
        while (goodSeedsInputSet.size() <= lastIndexInputSet) {
            performRun();
        }
        return goodSeedsInputSet.get(lastIndexInputSet);
    }

    private void performRun() {
        int targetValue = randTargets.nextInt(5, 12);
        long netCreatorSeed = randNetSeeds.nextLong();

        CopyValueGame game = new CopyValueGame(targetValue, netCreatorSeed);
        game.execute();
        if (game.passedGetTargetValue()) {
            goodSeedsGetTargetValue.add(netCreatorSeed);
        }
        if (game.passedInputSet()) {
            goodSeedsInputSet.add(netCreatorSeed);
        }
    }

}
