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
    protected QualifiedKey resolve(String simpleKey) {
        return new QualifiedKey(group + "." + simpleKey);
    }

    @Override
    protected <T> T doGet(QualifiedKey qualifiedKey) {
        return delegate.doGet(qualifiedKey);
    }

    @Override
    protected void doFix(QualifiedKey qualifiedKey, Object value) {
        delegate.doFix(qualifiedKey, value);
    }

    @Override
    public <T> T get(HyperDefinition<T> hyperDefinition) {
        return delegate.get(hyperDefinition.cloneWithName(resolve(hyperDefinition.getName()).get()));
    }
}
