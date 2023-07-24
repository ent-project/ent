package org.ent.hyper;

public abstract class HyperManager {
    public abstract <T> T get(NumericHyperDefinition<T> hyperDefinition);

    /**
     * Move to a subdirectory. Creates another view of the same HyperMangager
     * that interprets all property names as relative to the given subdirectory.
     * This allows to use the same property names in different contexts.
     */
    public HyperManager group(String group) {
        return new SubHyperManager(this, group);
    }
}
