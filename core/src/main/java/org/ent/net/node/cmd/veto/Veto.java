package org.ent.net.node.cmd.veto;

import org.ent.permission.Permissions;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ParameterizedValue;

public interface Veto extends ParameterizedValue {
    /*
    value bit pattern:
    |                 |                 |                 |                 |
      0 V O O O O O O   Z Z Z Z Y Y Y Y   X X X X C C C C   C C C C C C C N

    N - 'not' flag (inverts the condition)
    C - condition code
    X - accessor for the first parameter
    Y - accessor for the second parameter
    Z - accessor for the third parameter, all 0 in this case
    O - unused = 0
    V - veto flag = 1
     */

    int VETO_FLAG = 1 << 30;

    int getValue();

    boolean evaluate(Node base, Permissions permissions);

    String getShortName();

}
