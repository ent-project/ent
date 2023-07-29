package org.ent.dev.game.forwardarithmetic;

import org.apache.commons.rng.UniformRandomProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public StageBase(UniformRandomProvider randMaster) {
        this.randMaster = randMaster;
    }

    public void setTrialMaxEvaluations(int trialMaxEvaluations) {
        this.trialMaxEvaluations = trialMaxEvaluations;
    }

    public void setTrialMaxDuration(Duration trialMaxDuration) {
        this.trialMaxDuration = trialMaxDuration;
    }

    protected void runTrial(int indexTrial) {
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

    protected abstract void printRunInfo(Duration duration);

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
