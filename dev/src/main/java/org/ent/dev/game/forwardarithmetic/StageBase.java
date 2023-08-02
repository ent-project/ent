package org.ent.dev.game.forwardarithmetic;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.hyper.CollectingHyperManager;
import org.ent.hyper.HyperManager;
import org.ent.hyper.RemoteHyperManager;
import org.ent.net.util.RandomUtil;
import org.ent.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public abstract class StageBase<S> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected Integer trialMaxEvaluations;
    protected Duration trialMaxDuration;
    protected final UniformRandomProvider randMaster;

    protected Duration duration;
    protected Integer indexTrial;
    protected Integer indexEvaluation;

    private final List<S> solutions = new ArrayList<>();
    private int nextSolutionIndex;

    protected int numHit;
    protected int numEvaluation; // kind of the same as indexEvaluation, but explicitly keeping track of the total number

    public static abstract class StageFactory<SB extends StageBase<?>> {
        protected final Logger log = LoggerFactory.getLogger(getClass());

        protected final UniformRandomProvider randomTrials = RandomUtil.newRandom2(12345L);

        public void main(int numTrials) throws IOException {
            CollectingHyperManager hyperCollector = new CollectingHyperManager();
            registerHyperparameters(hyperCollector);
            RemoteHyperManager hyperManager = new RemoteHyperManager(hyperCollector.getHyperDefinitions());
            fixHyperparameters(hyperManager);
            runStudy(hyperManager, numTrials);
        }

        public void runStudy(RemoteHyperManager hyperManager, int numTrials) throws IOException {
            for (int indexTrial = 0; indexTrial < numTrials; indexTrial++) {
                Integer trialNumberRemote = hyperManager.suggest();

                StageBase dev = createStage(hyperManager, indexTrial);

                dev.runTrial(indexTrial);
                int hits = dev.numHit();
                double hitsPerMinute = hits * 60_000.0 / dev.duration().toMillis();
                log.info(" Hits per minute: {}", String.format("%.1f", hitsPerMinute));

                hyperManager.complete(trialNumberRemote, hitsPerMinute);
            }
        }

        public abstract SB createStage(RemoteHyperManager hyperManager, int indexTrial);

        public abstract void registerHyperparameters(HyperManager hyperCollector);

        public abstract void fixHyperparameters(HyperManager hyperManager);
    }

    public StageBase(UniformRandomProvider randMaster) {
        this.randMaster = randMaster;
    }

    public void setTrialMaxEvaluations(int trialMaxEvaluations) {
        this.trialMaxEvaluations = trialMaxEvaluations;
    }

    public void setTrialMaxDuration(Duration trialMaxDuration) {
        this.trialMaxDuration = trialMaxDuration;
    }

    public Duration duration() {
        return duration;
    }

    public int numHit() {
        return numHit;
    }

    public void runTrial(int indexTrial) {
        this.indexTrial = indexTrial;
        long startTime = System.nanoTime();
        indexEvaluation = 1;
        while (withinTimeLimit(startTime) && withinCountLimit(indexEvaluation)) {
            if (indexEvaluation % 20_000 == 0) {
                log.info("= i={} =", indexEvaluation);
                if (indexEvaluation % 100_000 == 0) {
                    printRunInfo(Duration.ofNanos(System.nanoTime() - startTime));
                }
            }
            nextEvaluation();
            indexEvaluation++;
            numEvaluation++;
        }
        this.duration = Duration.ofNanos(System.nanoTime() - startTime);
        printRunInfo(duration);
    }

    private boolean withinCountLimit(int indexEvaluation) {
        if (this.trialMaxEvaluations == null) {
            return true;
        }
        return indexEvaluation <= this.trialMaxEvaluations;
    }

    private boolean withinTimeLimit(long startTime) {
        if (this.trialMaxDuration == null) {
            return true;
        }
        Duration currentDuration = Duration.ofNanos(System.nanoTime() - startTime);
        return currentDuration.compareTo(this.trialMaxDuration) <= 0;
    }

    protected void printRunInfo(Duration duration) {
        log.info("#{} TOTAL DURATION: {}", indexTrial, duration);
        log.info(" hits: {}", Tools.rate(numHit, numEvaluation));
    }

    protected abstract void nextEvaluation();

    public S getNextSolution() {
        while (solutions.size() <= nextSolutionIndex) {
            nextEvaluation();
        }
        S solution = solutions.get(nextSolutionIndex);
        nextSolutionIndex++;
        return solution;
    }

    protected void submitSolution(S solution) {
        solutions.add(solution);
    }

}
