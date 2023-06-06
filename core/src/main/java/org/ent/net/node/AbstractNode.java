package org.ent.net.node;

import org.ent.net.AccessToken;
import org.ent.net.Net;

public abstract class AbstractNode implements Node {

    protected Net net;

    private Hub hub;

    protected AbstractNode(Net net) {
        this.net = net;
        this.hub = new Hub(this);
    }

    @Override
    public Net getNet() {
        return net;
    }

    @Override
    public void setNet(Net net) {
        this.net = net;
    }

    @Override
    public Hub getHub() {
        return hub;
    }

    @Override
    public void setHub(Hub hub) {
        this.hub = hub;
    }

    @Override
    public boolean permittedToSetValue(AccessToken accessToken) {
        return net.isPermittedToWrite(accessToken);
    }
}
