package org.ent.dev.game.enrich;

import org.ent.net.Net;

public class Piece {
    public Net net;
    public Long seed;
    public Boolean contributing;

    public Piece(Net net, Long seed) {
        this.net = net;
        this.seed = seed;
    }
}
