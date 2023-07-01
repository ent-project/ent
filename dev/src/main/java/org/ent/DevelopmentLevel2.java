package org.ent;

import org.ent.dev.variation.ArrowMixMerger;
import org.ent.net.Arrow;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.util.NetCopy2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Random;

import static org.ent.util.Tools.getHitsPerMinute;
import static org.ent.util.Tools.rate;

public class DevelopmentLevel2 {

    private static final Logger log = LoggerFactory.getLogger(DevelopmentLevel2.class);

    private final double frequencyFactor = 0.6;
    private final int maxSteps = 40;
    private final int attemptsPerUpstream = 500;

    private final Random randMaster;
    private final Random randTargetValue;
    private final DevelopmentLevel1a developmentLevel1a;
    private final DevelopmentLevel1b developmentLevel1b;
    private int numTotal, numHit, numRetained1a, numRetained1b, numRetainedBoth;
    private int numGenTotal, numGenHit;

    public static void main(String[] args) {
        DevelopmentLevel2 dev = new DevelopmentLevel2(new Random(0xFA1AFEL + 2));
        dev.run();
    }

    public DevelopmentLevel2(Random random) {
        this.randMaster = random;
        this.developmentLevel1a = new DevelopmentLevel1a(new Random(randMaster.nextLong()));
        this.developmentLevel1b = new DevelopmentLevel1b(new Random(randMaster.nextLong()));
        this.randTargetValue = new Random(randMaster.nextLong());
    }

    private void run() {
        long startTime = System.nanoTime();

        for (int i = 0; i < 200; i++) {
            next();
        }

        Duration duration = Duration.ofNanos(System.nanoTime() - startTime);
        log.info("");
        log.info("frequency factor: {}, max steps: {}, attempts per upstream: {}",
                frequencyFactor, maxSteps, attemptsPerUpstream);
        log.info("hit: {}, retained 1a: {}, retained 1b: {}",
                rate(numHit, numTotal),
                rate(numRetained1a, numTotal),
                rate(numRetained1b, numTotal));
        if (numRetainedBoth > 0) {
            log.info("retained both: {}", rate(numRetainedBoth, numTotal));
        }
        log.info("parent couples with offsping: {}", rate(numGenHit, numGenTotal));
        log.info("TOTAL DURATION: {}", duration);
        log.info("{} hits / min", getHitsPerMinute(numHit, duration));
    }

    private void next() {
        int targetValue = randTargetValue.nextInt(5, 17);

        numGenTotal++;
        DevelopmentLevel1a.Level1aSolution level1a = developmentLevel1a.getNextInputSetToTargetValue();
        DevelopmentLevel1b.Level1bSolution level1b = developmentLevel1b.getNextVerifierFinished();

        boolean foundSolution = false;
        for (int i = 0; i < attemptsPerUpstream; i++) {
            Net net1 = level1a.freshNet();
            Net net2 = level1b.freshNet();

            long mixSeed = randMaster.nextLong();
            ArrowMixMerger merger = new ArrowMixMerger(net1, net2, new Random(mixSeed), frequencyFactor);
            merger.execute();

            boolean investigate = numTotal == 73;
            if (investigate) {
                Net copy = NetCopy2.createCopy(net1);
                CopyValueGame game0 = new CopyValueGame(targetValue, copy, 8);
                TrimmingListener trimmingListener = new TrimmingListener(game0.getEnt().getNet().getNodes().size());
                game0.getEnt().getNet().addEventListener(trimmingListener);
                game0.execute();

                Net copy2 = NetCopy2.createCopy(net1);
                copy2.permitMarkerNode();
                for (Node node : copy2.getNodes()) {
                    for (Arrow arrow : node.getArrows()) {
                        if (trimmingListener.isDead(arrow)) {
                            arrow.setTarget(arrow.getOrigin(), Purview.DIRECT);
                        }
                    }
                }
                copy2.referentialGarbageCollection();
                CopyValueGame game2 = new CopyValueGame(targetValue, copy2, maxSteps);
                log.info("======= trimmed run =======");
                game2.setVerbose(true);
                game2.execute();
                log.info("=======");

            }
            CopyValueGame game = new CopyValueGame(targetValue, net1, maxSteps);
            if (investigate) {
                game.setVerbose(true);
            }
            game.execute();

            if (game.passedVerifierFinishedSuccessfully()) {
                numHit++;
                foundSolution = true;
                log.info("## verifier finished successfully! # {}", numTotal);
                break;
            } else if (game.passedInputSetToTargetValue() && game.passedVerifierFinished()) {
                log.info("# retained both but still failed");
                numRetainedBoth++;
            }
            if (game.passedInputSetToTargetValue()) {
                numRetained1a++;
            }
            if (game.passedVerifierFinished()) {
                numRetained1b++;
            }
            numTotal++;
        }
        if (foundSolution) {
            numGenHit++;
        }
    }
}
