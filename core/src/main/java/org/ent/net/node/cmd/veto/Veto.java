package org.ent.net.node.cmd.veto;

import org.ent.Ent;
import org.ent.net.node.Node;

public interface Veto {
    /*
    value bit pattern:
    |                 |                 |                 |                 |
      P V O O O O O O   Z Z Z Z Y Y Y Y   X X X X C C C C   C C C C C C C N

    N - 'not' flag (inverts the condition)
    C - condition code
    X - accessor for the first parameter
    Y - accessor for the second parameter
    Z - accessor for the third parameter, all 0 in this case
    O - unused = 0
    V - veto flag = 1
    P - portal flag = 0

     */

    int VETO_FLAG = 1 << 30;

    int getValue();

    boolean evaluate(Node base, Ent ent);

    String getShortName();

}
