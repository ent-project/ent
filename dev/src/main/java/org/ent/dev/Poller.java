package org.ent.dev;

import org.ent.dev.unit.Req;
import org.ent.dev.unit.Sup;
import org.ent.dev.unit.data.Data;

import java.util.ArrayDeque;
import java.util.Queue;

public class Poller implements Req {

    private Sup upstream;

    private final Queue<Data> queue = new ArrayDeque<>();

    @Override
    public void setUpstream(Sup upstream) {
        this.upstream = upstream;
    }

    public void poll() {
        upstream.requestNext();
    }

    @Override
    public void receiveNext(Data next) {
        queue.add(next);
    }

    public Data getFromQueue() {
        return queue.remove();
    }

    public boolean queueIsEmpty() {
        return queue.isEmpty();
    }
}
