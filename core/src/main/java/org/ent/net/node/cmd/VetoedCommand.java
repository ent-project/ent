package org.ent.net.node.cmd;

import org.ent.permission.Permissions;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.veto.Veto;
import org.ent.net.node.cmd.veto.Vetos;

public abstract class VetoedCommand implements Command {
    @Override
    public ExecutionResult execute(Node base, Permissions permissions) {
        int vetoValue = base.getLeftChild(permissions).getValue(permissions);
        Veto veto = Vetos.getByValue(vetoValue);
        if (veto != null) {
            boolean pass = veto.evaluate(base, permissions);
            if (!pass) {
                return ExecutionResult.NORMAL;
            }
        }
        return doExecute(base, permissions);
    }

    protected abstract ExecutionResult doExecute(Node base, Permissions permissions);

    @Override
    public String toString() {
        return "<" + getShortName() + ">";
    }
}
