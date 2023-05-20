package org.ent.net.node.cmd;

import org.ent.Ent;
import org.ent.net.node.Node;

public interface Command {
    /*
    value bit pattern:
    |                 |                 |                 |                 |
      0 0 1 1 0 0 1 1   Z Z Z Z Y Y Y Y   X X X X C C C C   C C C C C C C C

    C - command code
    X - accessor for the first parameter
    Y - accessor for the second parameter
    Z - accessor for the third parameter
    bit 25-32: some pattern to avoid collision with low numbers (not used for NopCommand)

     */
    int COMMAND_PATTERN = 0b110011 << 24;

    ExecutionResult execute(Node base, Ent ent);

    int getValue();

    String getShortName();

}
