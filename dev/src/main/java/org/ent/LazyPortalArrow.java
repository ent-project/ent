package org.ent;

import org.ent.net.AccessToken;
import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Purview;
import org.ent.net.node.Node;

import java.util.function.Supplier;

public class LazyPortalArrow implements Arrow {

    private Arrow delegate;

    private final Supplier<Arrow> arrowSupplier;

    public LazyPortalArrow(Supplier<Arrow> arrowSupplier) {
        this.arrowSupplier = arrowSupplier;
    }

    @Override
    public ArrowDirection getDirection() {
        throw new IllegalStateException();
    }

    @Override
    public Node getOrigin() {
        throw new IllegalStateException();
    }

    @Override
    public Node getTarget(Purview purview) {
        if (delegate == null) {
            createDelegate();
        }
        return delegate.getTarget(purview);
    }

    @Override
    public void setTarget(Node target, Purview purview) {
        if (delegate == null) {
            createDelegate();
        }
        delegate.setTarget(target, purview);
    }

    @Override
    public boolean permittedToSetTarget(Node target, AccessToken accessToken) {
        if (delegate == null) {
            createDelegate();
        }
        return delegate.permittedToSetTarget(target, accessToken);
    }

    private void createDelegate() {
        this.delegate = arrowSupplier.get();
    }
}
