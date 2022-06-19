package org.ent.dev.plan;

import org.ent.dev.stat.BinaryStat;
import org.ent.dev.unit.local.util.TypedFilterListener;
import org.ent.run.StepResult;

import java.util.EnumMap;

public class FailReasonRecorder extends TypedFilterListener<StepsFilterData> {
    private final EnumMap<StepResult, BinaryStat> failReasonStat;

    public FailReasonRecorder(EnumMap<StepResult, BinaryStat> failReasonStat) {
        super(new StepsFilterData());
        this.failReasonStat = failReasonStat;
    }

    @Override
    protected void successImpl(StepsFilterData data) {
        recordEvent(StepResult.SUCCESS);
    }

    @Override
    protected void failureImpl(StepsFilterData data) {
        recordEvent(data.getStepsExamResult().finalStepResult());
    }

    private void recordEvent(StepResult stepResult) {
        for (StepResult sr : StepResult.values()) {
            if (sr == stepResult) {
                failReasonStat.get(sr).addHit();
            } else {
                failReasonStat.get(sr).addMiss();
            }
        }
    }
}
