package org.ent.dev.unit.local.util;

import org.ent.dev.unit.data.DataProxy;

public interface ITypedProc<T extends DataProxy> {
    void accept(T data);
}
