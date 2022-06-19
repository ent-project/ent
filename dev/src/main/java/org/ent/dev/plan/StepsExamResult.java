package org.ent.dev.plan;

import org.ent.dev.RunSetup;
import org.ent.run.StepResult;

public record StepsExamResult(RunSetup runSetup, int steps, StepResult finalStepResult) {


}