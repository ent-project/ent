package org.ent;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.AccessToken;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;

public class PortalArrow implements Arrow {

    private Node target;

    private final Net net;

    private boolean readOnly;

    public PortalArrow(Net net) {
        this.net = net;
        this.target = net.getRoot();
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public PortalArrow setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
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
        return target;
    }

    @Override
    public void setTarget(Node target, Purview purview) {
        this.target = target;
    }

    @Override
    public boolean permittedToSetTarget(Node target, AccessToken accessToken) {
        if (readOnly) {
            return false;
        }
        return net == target.getNet();
    }
}
