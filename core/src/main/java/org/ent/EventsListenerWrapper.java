package org.ent;

import org.ent.net.Arrow;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.veto.Veto;

public class EventsListenerWrapper implements EntEventListener {
    EntEventListener delegate;

    public EntEventListener getDelegate() {
        return delegate;
    }

    public EventsListenerWrapper setDelegate(EntEventListener delegate) {
        this.delegate = delegate;
        return this;
    }

    @Override
    public void blockedByVeto(Veto veto) {
        if (delegate != null) {
            delegate.blockedByVeto(veto);
        }
    }

    @Override
    public void passedThroughVeto(Veto veto) {
        if (delegate != null) {
            delegate.passedThroughVeto(veto);
        }
    }

    @Override
    public void advancedThroughPortal(Node portalNode, Node domainPointer) {
        if (delegate != null) {
            delegate.advancedThroughPortal(portalNode, domainPointer);
        }
    }

    @Override
    public void domainBreachAttemptInSet(Arrow setter, Node target) {
        if (delegate != null) {
            delegate.domainBreachAttemptInSet(setter, target);
        }
    }
}
