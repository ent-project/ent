package org.ent.dev.plan;

import org.ent.dev.plan.DataProperties.PropReplicator;
import org.ent.dev.plan.DataProperties.PropVariabilityExamResult;
import org.ent.dev.unit.data.Data;
import org.ent.dev.unit.data.DataProxy;

public class VariabilityExamData extends DataProxy implements PropReplicator, PropVariabilityExamResult {

    public VariabilityExamData() {
    }

    public VariabilityExamData(Data delegate) {
        super(delegate);
    }
}
