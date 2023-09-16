package org.ent.net.node.cmd.operation;

import org.ent.permission.Permissions;
import org.ent.permission.WriteFacet;
import org.ent.net.Arrow;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;

public class SetOperation implements BiOperation {

    @Override
    public int getCode() {
        return Operations.CODE_SET_OPERATION;
    }

    @Override
    public ExecutionResult apply(Arrow setter, Arrow arrowToTarget, Permissions permissions) {
        if (permissions.noWrite(setter, WriteFacet.ARROW)) return ExecutionResult.ERROR;

        Node origin = setter.getOrigin();
        Permissions originPermissions = origin.getNet().getPermissions();

        Node target = arrowToTarget.getTarget(permissions);
        if (originPermissions.noPointTo(target)) return ExecutionResult.ERROR;

        setter.setTarget(target, permissions);
        return ExecutionResult.NORMAL;
    }

    @Override
    public String getShortName() {
        return "::";
    }

}
