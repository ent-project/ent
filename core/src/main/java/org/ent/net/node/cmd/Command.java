package org.ent.net.node.cmd;

import org.ent.permission.Permissions;
import org.ent.net.node.Node;

public interface Command extends ParameterizedValue {
    /*
    value bit pattern:
    |                 |                 |                 |                 |
      0 0 1 1 0 0 1 1   Z Z Z Z Y Y Y Y   X X X X F C C C   C C C C C C C C

    C - command code
    F - flag indicating whether the command is a final state
    X - accessor for the first parameter
    Y - accessor for the second parameter
    Z - accessor for the third parameter
    bit 25-32: some pattern to avoid collision with low numbers

     */
    int COMMAND_PATTERN = 0b110011 << 24;
    int COMMAND_PATTERN_AREA = 0b11111111 << 24;
    int IS_FINAL_FLAG = 0b1000 << 8;

    ExecutionResult execute(Node base, Permissions permissions);

    int getValue();

    String getShortName();

    default boolean isEval() {
        return false;
    }
}
