package org.ent;

import org.ent.listener.EntEventListener;
import org.ent.listener.MultiEntEventListener;
import org.ent.listener.NopEntEventListener;
import org.ent.net.Net;
import org.ent.permission.PermissionBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Ent {
    private Net net;

    private final List<Net> domains = new ArrayList<>();

    EntEventListener eventListener = new NopEntEventListener();

    public Ent() {
        domains.add(null); // reserve a spot for the primary Net
    }

    public Ent(Net net) {
        setNet(net);
    }

    public Net getNet() {
        return net;
    }

    public void setNet(Net net) {
        if (this.net != null) {
            net.setPermissions(this.net.getPermissions());
        }
        this.net = net;
        if (domains.isEmpty()) {
            domains.add(net);
        } else {
            this.domains.set(0, net);
        }
        net.setNetIndex(0);
    }

    public List<Net> getDomains() {
        return domains;
    }

    public void addDomain(Net net) {
        domains.add(net);
        net.setNetIndex(domains.size() - 1);
    }

    public void putPermissions(Consumer<PermissionBuilder> consumer) {
        PermissionBuilder permissionBuilder = new PermissionBuilder(domains.size());
        consumer.accept(permissionBuilder);
        for (int i = 0; i < domains.size(); i++) {
            Net domain = domains.get(i);
            domain.setPermissions(permissionBuilder.buildPermissions(i));
            domain.setExecutable(permissionBuilder.shouldBeExecutable(i));
        }
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
