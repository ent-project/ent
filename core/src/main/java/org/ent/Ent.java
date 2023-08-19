package org.ent;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;

import java.util.ArrayList;
import java.util.List;

public class Ent {
    private Net net;

    private Purview purview;

    private final List<Net> domains = new ArrayList<>();
    private final List<Arrow> portals = new ArrayList<>();

    EntEventListener eventListener = new NopEntEventListener();

    public Ent(Net net) {
        setNet(net);
    }

    public Net getNet() {
        return net;
    }

    public void setNet(Net net) {
        this.net = net;
        this.net.setCoreNet(true);
    }

    public List<Net> getDomains() {
        return domains;
    }

    public List<Arrow> getPortals() {
        return portals;
    }

    public Arrow getArrowMaybeThroughPortal(Node node, ArrowDirection direction, Purview purview) {
        int value = node.getValue(purview);
        int portalIndex = getPortalIndex(value, direction);
        if (isPortal(portalIndex)) {
            Arrow portal = portals.get(portalIndex);
            event().getArrowThroughPortal(node, direction, portal);
            return portal;
        } else {
            return node.getArrow(direction);
        }
    }

    public static int getPortalIndex(int value, ArrowDirection direction) {
        // "main" direction (left) gets the "better" (least significant bits) side of the int
        int halfValue = switch (direction) {
            case LEFT -> value & 0xFFFF;
            case RIGHT -> value >>> 16;
        };
        return halfValue ^ 0xFFFF;
    }

    public void addDomain(Net net) {
        if (net.isCoreNet()) {
            throw new IllegalArgumentException();
        }
        domains.add(net);
    }

    public int addPortal(Arrow portal) {
        int index = portals.size();
        portals.add(portal);
        return getPortalCode(index);
    }

    public static int getPortalCode(int index) {
        return index ^ 0xFFFF;
    }

    public boolean isPortal(int index) {
        return index >= 0 && index < portals.size();
    }

    public Ent setPurview(Purview purview) {
        if (this.purview != null) {
            throw new AssertionError("Trying to set purview " + purview + " but has unfinished purview " + this.purview);
        }
        this.purview = purview;
        return this;
    }

    public Ent clearPurview() {
        if (this.purview == null) {
            throw new AssertionError("trying to clear purview, but none is active");
        }
        this.purview = null;
        return this;
    }

    public EntEventListener getEventListener() {
        return eventListener;
    }

    public Ent addEventListener(EntEventListener eventListener) {
        if (this.eventListener.getClass() == NopEntEventListener.class) {
            this.eventListener = eventListener;
        } else if (this.eventListener instanceof MultiEntEventListener multiEntEventListener) {
            multiEntEventListener.addEntEventListener(eventListener);
        } else {
            EntEventListener current = this.eventListener;
            MultiEntEventListener multiEntEventListener = new MultiEntEventListener();
            multiEntEventListener.addEntEventListener(current);
            multiEntEventListener.addEntEventListener(eventListener);
            this.eventListener = multiEntEventListener;
        }
        return this;
    }

    public EntEventListener event() {
        return eventListener;
    }
}
