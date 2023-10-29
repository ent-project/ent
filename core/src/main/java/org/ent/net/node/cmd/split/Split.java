package org.ent.net.node.cmd.split;

import org.ent.permission.Permissions;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ParameterizedValue;

public interface Split extends ParameterizedValue {
    /*
    value bit pattern:
    |                 |                 |                 |                 |
      0 0 0 1 1 0 1 0   Z Z Z Z Y Y Y Y   X X X X C C C C   C C C C C C C N

    N - 'not' flag (inverts the condition)
    C - condition code
    X - accessor for the first parameter
    Y - accessor for the second parameter
    Z - accessor for the third parameter
    bit 25-32 - split identifier pattern
     */

    int SPLIT_PATTERN = 0b011010 << 24;
    int SPLIT_PATTERN_AREA = 0b11111111 << 24;

    int getValue();

    SplitResult evaluate(Node base, Permissions permissions);

    String getShortName();

}
