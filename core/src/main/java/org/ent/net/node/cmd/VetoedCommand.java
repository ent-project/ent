package org.ent.net.node.cmd;

import org.ent.permission.Permissions;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.veto.Veto;
import org.ent.net.node.cmd.veto.Vetos;

public abstract class VetoedCommand implements Command {
    @Override
    public ExecutionResult execute(Node base, Permissions permissions) {
        Node vetoNode = base.getLeftChild(permissions);
        int vetoValue = vetoNode.getValue(permissions);
        Veto veto = Vetos.getByValue(vetoValue);
        if (veto != null) {
            boolean pass = veto.evaluate(vetoNode, permissions);
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
