package org.ent.net.node.cmd.operation;

import org.ent.Ent;
import org.ent.net.Arrow;
import org.ent.net.node.cmd.ExecutionResult;

public interface MonoOperation {

    int getCode();

    ExecutionResult apply(Arrow handle, Ent ent);

    String getShortName();
}
