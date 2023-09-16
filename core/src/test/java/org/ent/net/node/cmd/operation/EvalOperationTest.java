package org.ent.net.node.cmd.operation;

import org.ent.Ent;
import org.ent.permission.Permissions;
import org.ent.Profile;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.ExecutionResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ent.util.NetBuilder.builder;
import static org.ent.util.NetBuilder.node;
import static org.ent.util.NetBuilder.unary;
import static org.ent.util.NetBuilder.value;

class EvalOperationTest {
    @BeforeAll
    static void setTestEnvironment() {
        Profile.setTest(true);
    }

    @Test
    void doApply() {
        Node x;
        Ent ent = builder().ent(unary(Operations.SET_VALUE_OPERATION, node(x = value(3), value(5))));
        EvalOperation evalOperation = new EvalOperation();

        ExecutionResult executionResult = evalOperation.doApply(ent.getNet().getRoot(), Permissions.DIRECT);

        assertThat(executionResult).isEqualTo(ExecutionResult.NORMAL);
        assertThat(x.getValue()).isEqualTo(5);
    }

    @Test
    void tryToEvalOtherEval() {
        Node x;
        Ent ent = builder().ent(unary(Operations.EVAL_OPERATION, unary(Operations.SET_VALUE_OPERATION, node(x = value(3), value(5)))));
        EvalOperation evalOperation = new EvalOperation();

        ExecutionResult executionResult = evalOperation.doApply(ent.getNet().getRoot(), Permissions.DIRECT);

        assertThat(executionResult).isEqualTo(ExecutionResult.ERROR);
        assertThat(x.getValue()).isEqualTo(3);
    }
}