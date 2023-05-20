package org.ent;

import org.ent.net.Arrow;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.veto.Veto;

public interface EntEventListener {

	void blockedByVeto(Veto veto);

	void passedThroughVeto(Veto veto);

	void advancedThroughPortal(Node portalNode, Node domainPointer);

    void domainBreachAttemptInSet(Arrow setter, Node target);
}
