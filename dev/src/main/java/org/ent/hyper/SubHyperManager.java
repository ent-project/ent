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
        return new QualifiedKey(resolved.extendPath(group), resolved.simpleKey);
    }

    @Override
    protected <T> T doGet(HyperDefinition<T> hyperDefinitionResolved) {
        return delegate.doGet(hyperDefinitionResolved);
    }

    @Override
    protected void doFix(QualifiedKey qualifiedKey, Object value, boolean override) {
        delegate.doFix(qualifiedKey, value, override);
    }
}
