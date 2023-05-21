package org.ent;

import org.ent.net.Arrow;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.veto.Veto;
import org.ent.run.StepResult;

public interface EntEventListener {

	// FIXME: obsolete
	void blockedByVeto(Veto veto);

	// FIXME: obsolete
	void passedThroughVeto(Veto veto);

	void advancedThroughPortal(Node portalNode, Node domainPointer);

    void domainBreachAttemptInSet(Arrow setter, Node target);

    void beforeCommandExecution(Node executionPointer, Command command);

	void afterCommandExecution(StepResult stepResult);

	void vetoEvaluation(Veto veto, Node node1, Node node2, boolean result);
}
