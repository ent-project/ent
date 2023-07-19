package org.ent.dev.randnet;

import org.apache.commons.rng.UniformRandomProvider;

public interface ValueDrawing {
    int drawValue(UniformRandomProvider rand);
}
