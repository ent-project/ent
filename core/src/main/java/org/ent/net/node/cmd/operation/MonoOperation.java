package org.ent.net.node.cmd.operation;

import org.ent.permission.Permissions;
import org.ent.net.Arrow;
import org.ent.net.node.cmd.ExecutionResult;

public interface MonoOperation {

    int getCode();

    ExecutionResult apply(Arrow handle, Permissions permissions);

    String getShortName();

    default boolean isEval() {
        return false;
    }
}
