package org.ent.util.builder;

import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.accessor.Accessors;

public enum ArgSingle {
    D(Accessors.L),
    L(Accessors.LL),
    R(Accessors.LR),
    LL(Accessors.LLL),
    LR(Accessors.LLR),
    RL(Accessors.LRL),
    RR(Accessors.LRR);

    private final Accessor accessor;

    ArgSingle(Accessor accessor) {
        this.accessor = accessor;
    }

    public Accessor accessor() {
        return accessor;
    }
}
