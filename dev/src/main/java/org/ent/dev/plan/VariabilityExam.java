package org.ent.dev.plan;

import com.google.common.annotations.VisibleForTesting;
import org.ent.Ent;
import org.ent.dev.ManagedRun;
import org.ent.dev.RunSetup;
import org.ent.dev.unit.local.TypedProc;
import org.ent.net.Net;
import org.ent.run.NetRunner;

public class VariabilityExam extends TypedProc<VariabilityExamData> {

    private final RunSetup runSetup;
    private VariabilityCollector collector;

    public VariabilityExam(RunSetup runSetup) {
        super(VariabilityExamData.class);
        this.runSetup = runSetup;
    }

    public RunSetup getRunSetup() {
        return runSetup;
    }

    @Override
    protected void doAccept(VariabilityExamData data) {
        Net netExamSpecimen = data.getReplicator().getNewSpecimen();
        Ent ent = new Ent(netExamSpecimen); // FIXME
        VariabilityExamResult result = examine(ent);

        data.setVariabilityExamResult(result);
    }

    private VariabilityExamResult examine(Ent ent) {
        collector = new VariabilityCollector();
        ent.getNet().addExecutionEventListener(collector);
        NetRunner runner = new NetRunner(ent);
        runner.setNetRunnerListener(collector);
        ManagedRun run = new ManagedRun(runSetup).withNetRunner(runner);
        run.perform();
        VariabilityRater rater = new VariabilityRater(collector);
        return new VariabilityExamResult(rater.getPoints());
    }

    @VisibleForTesting
    VariabilityCollector getCollector() {
        return collector;
    }
}
