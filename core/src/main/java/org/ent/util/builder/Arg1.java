package org.ent.util.builder;

import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.accessor.Accessors;

public enum Arg1 {
    D(Accessors.LL),
    L(Accessors.LLL),
    R(Accessors.LLR),
    LL(Accessors.LLLL),
    LR(Accessors.LLLR),
    RL(Accessors.LLRL),
    RR(Accessors.LLRR);

    private final Accessor accessor;

    Arg1(Accessor accessor) {
        this.accessor = accessor;
    }

    public Accessor accessor() {
        return accessor;
    }
}
