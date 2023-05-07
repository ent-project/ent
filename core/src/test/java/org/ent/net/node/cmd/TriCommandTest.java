package org.ent.net.node.cmd;

import org.ent.Environment;
import org.ent.net.Net;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.accessor.Accessors;
import org.ent.net.node.cmd.operation.Operations;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TriCommandTest {
    @BeforeAll
    static void setTestEnvironment() {
        Environment.setTest(true);
    }

    @Test
    void execute() {
        Net net = new Net();
        Node root = net.newRoot();
        Node arg1 = net.newCNode(7);
        Node arg2 = net.newCNode(5);
        Node parameters = net.newNode(arg1, arg2);
        root.setLeftChild(parameters);
        Command command = new TriCommand(Accessors.DIRECT, Accessors.LEFT, Accessors.RIGHT, Operations.PLUS);

        command.execute(root.getLeftArrow());

        assertThat(parameters.getValue()).isEqualTo(12);
        assertThat(arg1.getValue()).isEqualTo(7);
        assertThat(arg2.getValue()).isEqualTo(5);
    }
}