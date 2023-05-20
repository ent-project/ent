package org.ent;

import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;

import java.util.ArrayList;
import java.util.List;

public class Ent {
    private static final int PORTAL_MASK = 0b11111111111111111111111111111110;
    private Net net;

    private Purview purview;

    private final List<Net> domains;

    EventsListenerWrapper eventsListenerWrapper = new EventsListenerWrapper();

    public Ent(Net net) {
        this.net = net;
        domains = new ArrayList<>();
        domains.add(null);
        domains.add(null);
    }

    public Net getNet() {
        return net;
    }

    public void setNet(Net net) {
        this.net = net;
    }

    public Arrow advanceWithPortals(Node node, ArrowDirection direction) {
        int value = node.getValue();
        if (isPortal(value)) {
            int index = -value;
            Node domainPointer = getDomainRoot(index);
            event().advancedThroughPortal(node, domainPointer);
            return domainPointer.getArrow(direction);
        } else {
            return node.getArrow(direction);
        }
    }

    private Node getDomainRoot(int index) {
        Net domain = domains.get(index);
        if (domain == null) {
            domain = initializeDomain(index);
            domains.set(index, domain);
        }
        return domain.getRoot();
    }

    private Net initializeDomain(int index) {
        return switch (index) {
            case 0 -> {
                Net readDomain = new Net();
                Node readData = readDomain.newNode(0xbeef1234);
                readDomain.newRoot(readData, readData);
                yield readDomain;
            }
            case 1 -> {
                Net writeDomain = new Net();
                Node writeData = writeDomain.newNode();
                writeDomain.newRoot(writeData, writeData);
                yield writeDomain;
            }
            default -> throw new IllegalArgumentException();
        };
    }

    private static boolean isPortal(int value) {
        return (value & PORTAL_MASK) == PORTAL_MASK;
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
        return eventsListenerWrapper.getDelegate();
    }

    public Ent setEventListener(EntEventListener eventListener) {
        eventsListenerWrapper.setDelegate(eventListener);
        return this;
    }

    public EventsListenerWrapper event() {
        return eventsListenerWrapper;
    }

    public Ent setDomain(int i, Net domain) {
        domains.set(i, domain);
        return this;
    }
}
