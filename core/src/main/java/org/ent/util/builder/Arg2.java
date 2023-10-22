package org.ent.util.builder;

import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.accessor.Accessors;

public enum Arg2 {
    D(Accessors.LR),
    L(Accessors.LRL),
    R(Accessors.LRR),
    LL(Accessors.LRLL),
    LR(Accessors.LRLR),
    RL(Accessors.LRRL),
    RR(Accessors.LRRR);

    private final Accessor accessor;

    Arg2(Accessor accessor) {
        this.accessor = accessor;
    }

    public Accessor accessor() {
        return accessor;
    }
}
