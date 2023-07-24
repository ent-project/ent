package org.ent.dev.game.forwardarithmetic;

import org.apache.commons.rng.UniformRandomProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public abstract class StageBase {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected int epochSize;
    protected final UniformRandomProvider randMaster;
    protected Duration duration;

    public StageBase(UniformRandomProvider randMaster) {
        this.randMaster = randMaster;
    }

    public void setEpochSize(int epochSize) {
        this.epochSize = epochSize;
    }

    protected void runTrial(int indexTrial) {
        long startTime = System.nanoTime();
        for (int indexEpoch = 1; indexEpoch <= epochSize; indexEpoch++) {
            if (indexEpoch % 20_000 == 0) {
                log.info("= i={} =", indexEpoch);
                if (indexEpoch % 100_000 == 0) {
                    printRunInfo(Duration.ofNanos(System.nanoTime() - startTime));
                }
            }
            next(indexTrial, indexEpoch);
        }
        this.duration = Duration.ofNanos(System.nanoTime() - startTime);
        printRunInfo(duration);
    }

    protected abstract void printRunInfo(Duration duration);

    protected abstract void next(int indexTrial, int indexEpoch);


}
