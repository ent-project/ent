package org.ent.dev.unit.local.util;

import org.ent.dev.unit.data.DataProxy;
import org.ent.dev.unit.local.TypedProc;

public class TypedProcWrapper<T extends DataProxy> extends TypedProc<T> {

    private final ITypedProc<T> delegate;

    public TypedProcWrapper(Class<T> accessorType, ITypedProc<T> delegate) {
        super(accessorType);
        this.delegate = delegate;
    }

    @Override
    protected void doAccept(T data) {
        delegate.accept(data);
    }
}
