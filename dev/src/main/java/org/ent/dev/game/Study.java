package org.ent.dev.game;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.dev.hyper.CollectingHyperManager;
import org.ent.dev.hyper.HyperManager;
import org.ent.dev.hyper.RemoteHyperManager;
import org.ent.net.util.RandomUtil;
import org.ent.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A Study consists of a number of Trials.
 * A Trial consists of a number of Evaluations.
 * <p>
 * In a scenario of Hyperparameter optimization, a Study will choose a new set of Hyperparameters
 * for each of its Trials. A Trial is limited by time or by a maximum number of Evaluations,
 * or both (as is appropriate to judge the quality of the Hyperparameter choice).
 * <p>
 * An Evaluation computes a solution (hit) and submits it: {@link Study#submitSolution(Object)}.
 * The measure of quality for a Trial is the number of hits per time.
 */
public abstract class Study<S> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected Integer trialMaxEvaluations;
    protected Duration trialMaxDuration;
    protected final UniformRandomProvider randMaster;

    protected Duration duration;
    protected Integer indexTrial;
    protected Integer indexEvaluation;

    protected final List<S> solutions = new ArrayList<>();
    private int nextSolutionIndex;

    protected int numHit;
    protected int numEvaluation; // kind of the same as indexEvaluation, but explicitly keeping track of the total number

    public static abstract class StudyFactory<SB extends Study<?>> {
        protected final Logger log = LoggerFactory.getLogger(getClass());

        protected final UniformRandomProvider randomTrials = RandomUtil.newRandom(12345L);

        public void start(int numTrials) throws IOException {
            CollectingHyperManager hyperCollector = new CollectingHyperManager();
            registerHyperparameters(hyperCollector);
            RemoteHyperManager hyperManager = new RemoteHyperManager(hyperCollector.getHyperDefinitions());
            fixHyperparameters(hyperManager);
            if (hyperManager.requiresRemoteCall()) {
                hyperManager.startStudy(getStudyName());
            }
            runStudy(hyperManager, numTrials);
        }

        protected String getStudyName() {
            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return "study-%s-%s".formatted(getStageClass().getSimpleName(), date);
        }

        private Class<SB> getStageClass() {
            Type superclass = getClass().getGenericSuperclass();
            ParameterizedType parameterized = (ParameterizedType) superclass;
            @SuppressWarnings("unchecked")
            Class<SB> result = (Class<SB>) parameterized.getActualTypeArguments()[0];
            return result;
        }

        public void runStudy(RemoteHyperManager hyperManager, int numTrials) throws IOException {
            for (int indexTrial = 0; indexTrial < numTrials; indexTrial++) {
                Integer trialNumberRemote = hyperManager.suggest();

                SB dev = createStage(hyperManager, indexTrial);

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

    public Study(UniformRandomProvider randMaster) {
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

    public void initializeTrial(Integer indexTrial) {
        this.indexTrial = indexTrial;
        this.indexEvaluation = 1;
    }

    public void runTrial(int indexTrial) {
        initializeTrial(indexTrial);
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
            indexEvaluation++;
        }
        S solution = solutions.get(nextSolutionIndex);
        solutions.set(nextSolutionIndex, null);
        nextSolutionIndex++;
        return solution;
    }

    protected void submitSolution(S solution) {
        solutions.add(solution);
    }

    protected String getStoryId() {
        if (indexTrial != null) {
            return "%s-%s".formatted(indexTrial, indexEvaluation);
        } else {
            return UUID.randomUUID().toString();
        }
    }

}
