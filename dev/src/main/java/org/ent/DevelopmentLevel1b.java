package org.ent;

import org.ent.dev.randnet.RandomNetCreator;
import org.ent.dev.variation.ArrowMixMutation;
import org.ent.net.Net;
import org.ent.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Random;

public class DevelopmentLevel1b {
    private static final Logger log = LoggerFactory.getLogger(DevelopmentLevel1b.class);

    private final DevelopmentLevel0 developmentLevel0;
    private final Random randMaster;
    private final Random randTargetValue;

    public static void main(String[] args) {
        DevelopmentLevel1b developmentLevel1b = new DevelopmentLevel1b();
        developmentLevel1b.next();
    }

    public DevelopmentLevel1b() {
        this.developmentLevel0 = new DevelopmentLevel0();
        this.randMaster = new Random(0xFA1AFEL);
        this.randTargetValue = new Random(randMaster.nextLong());
    }

    private void next() {
        long startTime = System.nanoTime();

        int numSource = 0;
        int numSourceSuccess = 0;
        int numTotal = 0;
        int numHit = 0;

        for (int i = 0; i < 500; i++) {
            numSource++;
            log.info("== Source {} ==", numSource);
            int targetValue = randTargetValue.nextInt(5, 17);
            CopyValueGame game0 = developmentLevel0.nextEvalFlowOnVerifierRoot();
            if (game0.passedVerifierFinished()) {
                numSourceSuccess++;
                numHit++;
                continue; // just interested to see the improvements from mutation
            }
            long seed = game0.getNetCreatorSeed();

            boolean foundSolution = false;
            for (int j = 0; j < 300; j++) {

                RandomNetCreator netCreator = new RandomNetCreator(new Random(seed), CopyValueGame.drawing);
                Net net = netCreator.drawNet();

//                NetFormatter formatter = new NetFormatter();
//                log.info("before: {}", formatter.format(net));

                long mixSeed = randMaster.nextLong();
                ArrowMixMutation mutation = new ArrowMixMutation(net, new Random(mixSeed));
                mutation.execute();
//                log.info("after:  {}", formatter.format(net));

                CopyValueGame game = new CopyValueGame(targetValue, net);
                game.execute();

                if (!game.passedEvalFlowOnVerifierRoot()) {
//                    log.info("# regression, no longer eval flow on verifier root");
                } else {
                    if (game.passedVerifierFinished()) {
                        log.info("# verifier finished!");
                        numHit++;
                        foundSolution = true;
                        break;
                    } else {
//                        log.info("# ()");
                    }
                }
                numTotal++;
            }
            if (!foundSolution) {
                log.info("## found no solution");
            } else {
                numSourceSuccess++;
            }
        }

        Duration duration = Duration.ofNanos(System.nanoTime() - startTime);
        log.info("sources with solution: {}", rate(numSourceSuccess, numSource));
        log.info("hits total: {}", rate(numHit, numTotal));
        log.info("TOTAL DURATION: {}", duration);
        log.info("{} hits / min", Tools.getHitsPerMinute(numHit, duration));
    }

    private String rate(int count, int total) {
        return "%d / %d (%.2f %%)".formatted(count, total, ((double) count) / total * 100);
    }
}
