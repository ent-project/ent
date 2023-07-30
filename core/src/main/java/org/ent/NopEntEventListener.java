package org.ent;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.operation.TriValueOperation;
import org.ent.net.node.cmd.veto.Veto;
import org.ent.run.StepResult;

public class NopEntEventListener implements EntEventListener {
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

    @Override
    public void getArrowThroughPortal(Node node, ArrowDirection direction, Arrow portal) {
        // do nothing
    }

    @Override
    public void evalFloatOperation(Node node) {
        // do nothing
    }

    @Override
    public void transverValue(Node nodeSource, Node nodeTarget) {
        // do nothing
    }

    @Override
    public void triValueOperation(Node node1, Node node2, Node node3, TriValueOperation operation) {
        // do nothing
    }
}
