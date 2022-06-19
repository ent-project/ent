package org.ent.dev.unit.local.util;

import org.ent.dev.unit.data.Data;
import org.ent.dev.unit.data.DataProxy;

public abstract class TypedFilterListener<T extends DataProxy> implements FilterListener {

    private final T accessor;

    protected TypedFilterListener(T accessor) {
        this.accessor = accessor;
    }

    protected abstract void successImpl(T data);

    protected abstract void failureImpl(T data);

    @Override
    public void success(Data data) {
        accessor.setDelegate(data);
        successImpl(accessor);
    }

    @Override
    public void failure(Data data) {
        accessor.setDelegate(data);
        failureImpl(accessor);
    }
}
