package org.ent.net.node.cmd;

import org.ent.Ent;
import org.ent.net.Arrow;
import org.ent.net.Purview;
import org.ent.net.node.cmd.veto.Veto;
import org.ent.net.node.cmd.veto.Vetos;

public abstract class VetoedCommand implements Command {
    @Override
    public ExecutionResult execute(Arrow parameters, Ent ent) {
        int vetoValue = parameters.getTarget(Purview.COMMAND).getValue();
        Veto veto = Vetos.getByValue(vetoValue);
        if (veto != null) {
            boolean pass = veto.evaluate(parameters, ent);
            if (!pass) {
                ent.event().blockedByVeto(veto);
                return ExecutionResult.NORMAL;
            } else {
                ent.event().passedThroughVeto(veto);
            }
        }
        return doExecute(parameters, ent);
    }

    protected abstract ExecutionResult doExecute(Arrow parameters, Ent ent);

    @Override
    public String toString() {
        return "<" + getShortName() + ">";
    }
}
