package org.ent.dev.unit.local.util;

import org.ent.dev.unit.data.Data;

public interface FilterListener {
    void success(Data data);

    void failure(Data data);
}
