package org.ent.dev.unit.local;

import org.ent.dev.unit.data.Data;

public interface FilterListener {
    void success(Data data);

    void failure(Data data);
}
