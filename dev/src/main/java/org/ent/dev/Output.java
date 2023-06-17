package org.ent.dev;

import org.ent.Ent;
import org.ent.dev.plan.StepsExamResult;
import org.ent.dev.unit.local.TypedProc;
import org.ent.net.io.formatter.NetFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Output extends TypedProc<OutputData> {
    private static final Logger log = LoggerFactory.getLogger(Output.class);

    String prefix;

    public Output(String prefix) {
        super(OutputData.class);
        this.prefix = prefix;
    }

    @Override
    public void doAccept(OutputData input) {
        Ent ent = input.getEnt();
        StepsExamResult stepsExamResult = input.getStepsExamResult();
        if (log.isTraceEnabled()) {
            log.trace("{}#{} [{}] {}", prefix, input.getSerialNumber(), stepsExamResult.steps(), new NetFormatter().format(ent));
        }
    }

}
