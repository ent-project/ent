package org.ent;

import org.ent.dev.randnet.RandomNetCreator;
import org.ent.dev.variation.ArrowMixMutation;
import org.ent.net.Net;
import org.ent.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DevelopmentLevel1b {
    private static final Logger log = LoggerFactory.getLogger(DevelopmentLevel1b.class);

    private final int maxStepsLevel0 = 20;
    private final int maxSteps = 20;
    private final int attemptsPerUpstream = 250;
    private final int numberOfNodes = 20;
    private final double frequencyFactor = 1.0;


    private final DevelopmentLevel0 developmentLevel0;
    private final Random randMaster;
    private final Random randTargetValue;
    private final List<Level1bSolution> goodSeeds = new ArrayList<>();
    private int nextIndexGoodSeeds;
    private int numUpstreamTotal;
    private int numUpstreamDirectHit;
    private int numTotal, numHit, numDegraded, numRetained;
    private int numGenTotal, numGenHit, numGenFail;


    public interface Level1bSolution {
        Net freshNet();

        CopyValueGame getFinishedGame();
    }

    public record Level1bSolutionDirect(CopyValueGame game) implements Level1bSolution {
        @Override
        public Net freshNet() {
            RandomNetCreator netCreator = new RandomNetCreator(game.getNumberOfNodes(), new Random(game.getNetCreatorSeed()), CopyValueGame.drawing);
            return netCreator.drawNet();
        }

        @Override
        public CopyValueGame getFinishedGame() {
            return game;
        }
    }

    public class Level1bSolutionMutating implements Level1bSolution {

        private final CopyValueGame upstream;
        private final long mixSeed;
        private final CopyValueGame game;

        public Level1bSolutionMutating(CopyValueGame upstream, long mixSeed, CopyValueGame game) {
            this.upstream = upstream;
            this.mixSeed = mixSeed;
            this.game = game;
        }

        @Override
        public Net freshNet() {
            RandomNetCreator netCreator = new RandomNetCreator(numberOfNodes, new Random(upstream.getNetCreatorSeed()), CopyValueGame.drawing);
            Net net = netCreator.drawNet();

            ArrowMixMutation mutation = new ArrowMixMutation(frequencyFactor, net, new Random(mixSeed));
            mutation.execute();

            return net;
        }

        @Override
        public CopyValueGame getFinishedGame() {
            return game;
        }
    }

    public static void main(String[] args) {
        DevelopmentLevel1b developmentLevel1b = new DevelopmentLevel1b(new Random(0xFA1AFEL +1));
        developmentLevel1b.run();
    }

    public DevelopmentLevel1b(Random random) {
        this.randMaster = random;
        this.randTargetValue = new Random(randMaster.nextLong());
        this.developmentLevel0 = new DevelopmentLevel0(maxStepsLevel0, numberOfNodes, new Random(randMaster.nextLong()));
    }

    public Level1bSolution getNextVerifierFinished() {
        while (goodSeeds.size() <= nextIndexGoodSeeds) {
            next();
        }
        Level1bSolution result = goodSeeds.get(nextIndexGoodSeeds);
        nextIndexGoodSeeds++;
        return result;
    }

    private void run() {
        long startTime = System.nanoTime();

        for (int i = 0; i < 400; i++) {
            if (i % 20 == 0) {
                log.info("## i = {}", i);
            }
            getNextVerifierFinished();
        }

        Duration duration = Duration.ofNanos(System.nanoTime() - startTime);
        log.info("");
        log.info("number of nodes: {}, max steps lvl0/lvl1b: {}/{}, attempts per upstream: {}, frequency factor: {}",
                numberOfNodes, maxStepsLevel0, maxSteps, attemptsPerUpstream, frequencyFactor);
        log.info("hit: {}, miss: {}, retained: {}",
                rate(numHit, numTotal),
                rate(numDegraded, numTotal),
                rate(numRetained, numTotal)
        );
        log.info("upstream direct hit: {}", rate(numUpstreamDirectHit, numUpstreamTotal));
        log.info("children per parent - hit: {}, none: {}",
                rate(numGenHit, numGenTotal),
                rate(numGenFail, numGenTotal));
        log.info("TOTAL DURATION: {}", duration);
        log.info("upstream: {} hits / min, generated: {} hits / min, total: {} hits / min",
                Tools.getHitsPerMinute(numUpstreamDirectHit, duration),
                Tools.getHitsPerMinute(numHit, duration),
                Tools.getHitsPerMinute(numHit + numUpstreamDirectHit, duration));
    }

    private void next() {
        numUpstreamTotal++;
        CopyValueGame upstream = developmentLevel0.nextEvalFlowOnVerifierRoot();
        if (upstream.passedVerifierFinished()) {
            numUpstreamDirectHit++;
            goodSeeds.add(new Level1bSolutionDirect(upstream));
            return;
        }

        numGenTotal++;
        long seed = upstream.getNetCreatorSeed();

        int targetValue = randTargetValue.nextInt(5, 17);
        boolean foundSolution = false;
        for (int i = 0; i < attemptsPerUpstream; i++) {

            RandomNetCreator netCreator = new RandomNetCreator(numberOfNodes, new Random(seed), CopyValueGame.drawing);
            Net net = netCreator.drawNet();

//                NetFormatter formatter = new NetFormatter();
//                log.info("before: {}", formatter.format(net));

            long mixSeed = randMaster.nextLong();
            ArrowMixMutation mutation = new ArrowMixMutation(frequencyFactor, net, new Random(mixSeed));
            mutation.execute();
//                log.info("after:  {}", formatter.format(net));

            CopyValueGame game = new CopyValueGame(targetValue, net, maxSteps);
            game.execute();

            if (!game.passedEvalFlowOnVerifierRoot()) {
                numDegraded++;
            } else {
                numRetained++;
                if (game.passedVerifierFinished()) {
                    log.info("# verifier finished!");
                    numHit++;
                    foundSolution = true;
                    goodSeeds.add(new Level1bSolutionMutating(upstream, mixSeed, game));
                    break;
                }
            }
            numTotal++;
        }
        if (!foundSolution) {
            numGenFail++;
        } else {
            numGenHit++;
        }
    }

    private String rate(int count, int total) {
        return "%d / %d (%.2f %%)".formatted(count, total, ((double) count) / total * 100);
    }
}
