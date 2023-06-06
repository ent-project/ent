package org.ent.net.node.cmd;

import org.ent.Ent;
import org.ent.net.AccessToken;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.veto.Veto;
import org.ent.net.node.cmd.veto.Vetos;

public abstract class VetoedCommand implements Command {
    @Override
    public ExecutionResult execute(Node base, Ent ent, AccessToken accessToken) {
        int vetoValue = base.getLeftChild(Purview.COMMAND).getValue();
        Veto veto = Vetos.getByValue(vetoValue);
        if (veto != null) {
            boolean pass = veto.evaluate(base, ent);
            if (!pass) {
                ent.event().blockedByVeto(veto);
                return ExecutionResult.NORMAL;
            } else {
                ent.event().passedThroughVeto(veto);
            }
        }
        return doExecute(base, ent, accessToken);
    }

    protected abstract ExecutionResult doExecute(Node base, Ent ent, AccessToken accessToken);

    @Override
    public String toString() {
        return "<" + getShortName() + ">";
    }
}
