package org.ent;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.veto.Veto;
import org.ent.run.StepResult;

import java.util.ArrayList;
import java.util.List;

public class MultiEntEventListener implements EntEventListener {

    List<EntEventListener> eventListeners = new ArrayList<>();

    public void addEntEventListener(EntEventListener entEventListener) {
        eventListeners.add(entEventListener);
    }

    @Override
    public void blockedByVeto(Veto veto) {
        for (EntEventListener eventListener : eventListeners) {
            eventListener.blockedByVeto(veto);
        }
    }

    @Override
    public void passedThroughVeto(Veto veto) {
        for (EntEventListener eventListener : eventListeners) {
            eventListener.passedThroughVeto(veto);
        }
    }

    @Override
    public void advancedThroughPortal(Node portalNode, Node domainPointer) {
        for (EntEventListener eventListener : eventListeners) {
            eventListener.advancedThroughPortal(portalNode, domainPointer);
        }
    }

    @Override
    public void domainBreachAttemptInSet(Arrow setter, Node target) {
        for (EntEventListener eventListener : eventListeners) {
            eventListener.domainBreachAttemptInSet(setter, target);
        }
    }

    @Override
    public void beforeCommandExecution(Node executionPointer, Command command) {
        for (EntEventListener eventListener : eventListeners) {
            eventListener.beforeCommandExecution(executionPointer, command);
        }
    }

    @Override
    public void afterCommandExecution(StepResult stepResult) {
        for (EntEventListener eventListener : eventListeners) {
            eventListener.afterCommandExecution(stepResult);
        }
    }

    @Override
    public void vetoEvaluation(Veto veto, Node node1, Node node2, boolean result) {
        for (EntEventListener eventListener : eventListeners) {
            eventListener.vetoEvaluation(veto, node1, node2, result);
        }
    }

    @Override
    public void getArrowThroughPortal(Node node, ArrowDirection direction, Arrow portal) {
        for (EntEventListener eventListener : eventListeners) {
            eventListener.getArrowThroughPortal(node, direction, portal);
        }
    }

    @Override
    public void evalFloatOperation(Node node) {
        for (EntEventListener eventListener : eventListeners) {
            eventListener.evalFloatOperation(node);
        }
    }
}
