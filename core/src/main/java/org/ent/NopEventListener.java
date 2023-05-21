package org.ent;

import org.ent.net.Arrow;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.veto.Veto;
import org.ent.run.StepResult;

public class NopEventListener implements EntEventListener {
    @Override
    public void blockedByVeto(Veto veto) {
        // do nothing
    }

    @Override
    public void passedThroughVeto(Veto veto) {
        // do nothing
    }

    @Override
    public void advancedThroughPortal(Node portalNode, Node domainPointer) {
        // do nothing
    }

    @Override
    public void domainBreachAttemptInSet(Arrow setter, Node target) {
        // do nothing
    }

    @Override
    public void beforeCommandExecution(Node executionPointer, Command command) {
        // do nothing
    }

    @Override
    public void afterCommandExecution(StepResult stepResult) {
        // do nothing
    }

    @Override
    public void vetoEvaluation(Veto veto, Node node1, Node node2, boolean result) {
        // do nothing
    }
}
