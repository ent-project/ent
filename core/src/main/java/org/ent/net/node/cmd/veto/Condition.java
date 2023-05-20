package org.ent.net.node.cmd.veto;

import org.ent.Ent;
import org.ent.net.Arrow;

public interface Condition {

    int getValue();

    boolean evaluate(Arrow parameters, Ent ent);

    String getShortNameAscii();
}
