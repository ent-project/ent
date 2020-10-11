package org.ent.dev.plan;

import org.ent.ExecutionEventListener;
import org.ent.dev.ManagedRun;
import org.ent.dev.RunSetup;
import org.ent.dev.unit.local.TypedProc;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.node.CNode;
import org.ent.net.node.Node;

public class VariabilityExam extends TypedProc<VariabilityExamData> {

    private final RunSetup runSetup;

    public VariabilityExam(RunSetup runSetup) {
        super(new VariabilityExamData());
        this.runSetup = runSetup;
    }

    private static class VariabilityEvaluator implements ExecutionEventListener {

        @Override
        public void fireExecutionStart() {

        }

        @Override
        public void fireGetChild(Node n, ArrowDirection arrowDirection) {

        }

        @Override
        public void fireSetChild(Node from, ArrowDirection arrowDirection, Node to) {

        }

        @Override
        public void fireNewNode(Node n) {

        }

        @Override
        public void fireCommandExecuted(CNode cmd) {

        }
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
        VariabilityEvaluator evaluator = new VariabilityEvaluator();
        ManagedRun run = new ManagedRun(runSetup, evaluator).withNet(net);
        run.perform();
        int steps = run.getNoSteps();
        return new VariabilityExamResult();
    }
}
