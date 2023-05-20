package org.ent.net.node.cmd.veto;

import org.ent.Ent;
import org.ent.net.Arrow;

public interface Veto {

    int VETO_FLAG = 1 << 30;

    int getValue();

    boolean evaluate(Arrow parameters, Ent ent);

    String getShortName();

}
