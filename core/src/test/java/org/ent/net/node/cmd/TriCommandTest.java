package org.ent.net.node.cmd;

import org.ent.TestBase;
import org.ent.net.Net;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.accessor.Accessors;
import org.ent.net.node.cmd.operation.Operations;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ent.net.node.cmd.veto.Conditions.GREATER_THAN_CONDITION;
import static org.ent.util.NetBuilder.builder;
import static org.ent.util.NetBuilder.node;
import static org.ent.util.NetBuilder.unary;
import static org.ent.util.NetBuilder.value;

class TriCommandTest extends TestBase {

    @Test
    void execute() {
        Node parameters, arg1, arg2;
        Net net = builder().net(unary(parameters = node(arg1 = value(7), arg2 = value(5))));
        Command command = new TriCommand(Operations.PLUS_OPERATION, Accessors.L, Accessors.LL, Accessors.LR);

        command.execute(net.getRoot(), net.getPermissions());

        assertThat(parameters.getValue()).isEqualTo(12);
        assertThat(arg1.getValue()).isEqualTo(7);
        assertThat(arg2.getValue()).isEqualTo(5);
    }


    @Test
    void execute_withVeto_pass() {
        Node i;
        Net net = builder().net(unary(node(GREATER_THAN_CONDITION, i = value(7), value(0))));
        Command command = new TriCommand(Operations.PLUS_OPERATION, Accessors.LL, Accessors.LL, Accessors.LL); // i = i + i

        command.execute(net.getRoot(), net.getPermissions());

        assertThat(i.getValue()).isEqualTo(14);
    }

    @Test
    void execute_withVeto_reject() {
        Node i;
        Net net = builder().net(unary(node(GREATER_THAN_CONDITION, i = value(7), value(1000))));
        Command command = new TriCommand(Operations.PLUS_OPERATION, Accessors.LL, Accessors.LL, Accessors.LL); // i = i + i

        command.execute(net.getRoot(), net.getPermissions());

        assertThat(i.getValue()).isEqualTo(7);
    }
}