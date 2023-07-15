package org.ent;

import org.ent.net.AccessToken;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;

public class RootPortalArrow extends PortalArrow {
    public RootPortalArrow(Net net) {
        super(net);
    }

    public RootPortalArrow(Net net, Node target) {
        super(net, target);
    }

    @Override
    public void setTarget(Node target, Purview purview, AccessToken token) {
        super.setTarget(target, purview);
        if (net.isPermittedToSetRoot(token)) {
            net.setRoot(target);
        }
    }
}
