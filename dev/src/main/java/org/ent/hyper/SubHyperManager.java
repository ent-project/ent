package org.ent.hyper;

import java.util.Objects;

public class SubHyperManager extends HyperManager {

    private final HyperManager delegate;
    private final String group;

    public SubHyperManager(HyperManager delegate, String group) {
        Objects.requireNonNull(group);
        this.delegate = delegate;
        this.group = group;
    }

    @Override
    public <T> T get(NumericHyperDefinition<T> hyperDefinition) {
        return delegate.get(hyperDefinition.group(this.group));
    }

}
