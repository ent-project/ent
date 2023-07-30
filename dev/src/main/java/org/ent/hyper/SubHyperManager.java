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
        QualifiedKey resolved = delegate.resolve(simpleKey);
        return new QualifiedKey(group + "." + resolved.get());
    }

    @Override
    protected <T> T doGet(HyperDefinition<T> hyperDefinitionResolved) {
        return delegate.doGet(hyperDefinitionResolved);
    }

    @Override
    protected void doFix(QualifiedKey qualifiedKey, Object value) {
        delegate.doFix(qualifiedKey, value);
    }
}
