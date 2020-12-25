package org.ent.dev.plan;

import org.ent.dev.ManagedRun;
import org.ent.dev.RunSetup;
import org.ent.dev.unit.local.TypedProc;
import org.ent.net.DefaultNetController;
import org.ent.net.Net;
import org.ent.net.NetController;
import org.ent.run.NetRunner;

public class VariabilityExam extends TypedProc<VariabilityExamData> {

    private final RunSetup runSetup;

    public VariabilityExam(RunSetup runSetup) {
        super(new VariabilityExamData());
        this.runSetup = runSetup;
    }

    public RunSetup getRunSetup() {
        return runSetup;
    }

    @Override
    protected void doAccept(VariabilityExamData data) {
        Net netExamSpecimen = data.getReplicator().getNewSpecimen();
        VariabilityExamResult result = examine(netExamSpecimen);

        data.setVariabilityExamResult(result);
    }

    private VariabilityExamResult examine(Net net) {
        VariabilityCollector collector = new VariabilityCollector();
        NetController controller = new DefaultNetController(net, collector);
        NetRunner runner = new NetRunner(net, controller);
        runner.setNetRunnerListener(collector);
        ManagedRun run = new ManagedRun(runSetup, collector).withNetRunner(runner);
        run.perform();
        VariabilityRater rater = new VariabilityRater(collector);
        return new VariabilityExamResult(rater.getPoints());
    }
}
