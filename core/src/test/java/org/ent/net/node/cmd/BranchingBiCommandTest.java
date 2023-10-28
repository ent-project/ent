package org.ent.net.node.cmd;

import org.ent.Profile;
import org.ent.net.Net;
import org.ent.net.node.cmd.accessor.Accessors;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.net.node.cmd.veto.Conditions;
import org.ent.net.node.cmd.veto.Veto;
import org.ent.net.node.cmd.veto.Vetos;
import org.ent.run.EntRunner;
import org.ent.util.builder.EntBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ent.util.builder.NodeTemplate.node;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class BranchingBiCommandTest {

    @BeforeAll
    static void setTestEnvironment() {
        Profile.setTest(true);
    }

    private static Stream<Arguments> branches() {
        Veto veto = Vetos.get(Conditions.SAME_VALUE_CONDITION, Accessors.LL, Accessors.LR);
        return Stream.of(
                arguments(veto.getValue(), 7, 7, Commands.CONCLUSION_SUCCESS),
                arguments(veto.getValue(), 7, 8, Commands.CONCLUSION_FAILURE),
                arguments(0, 7, 7, Commands.NOP),
                arguments(0, 7, 8, Commands.NOP)
        );
    }

    @ParameterizedTest
    @MethodSource("branches")
    void firstBranch(int conditionCode, int firstValue, int secondValue, Command expectedRoot) {
        EntBuilder builder = new EntBuilder();
        Command branchingSet = Commands.getBranching(Operations.SET_OPERATION, Accessors.R, Accessors.LRL, Accessors.LRR);
        builder.chain(
                node().setRoot().command(branchingSet)
                        .left(node().value(conditionCode)
                                .left(node()
                                        .left(node().value(firstValue))
                                        .right(node().value(secondValue)))
                                .right(node()
                                        .left(node().command(Commands.CONCLUSION_SUCCESS))
                                        .right(node().command(Commands.CONCLUSION_FAILURE))
                                )
                        )
                        .right(node().command(Commands.NOP))
        );
        Net net = builder.build();
        EntRunner runner = new EntRunner(net);

        runner.step();

        assertThat(net.getRoot().getValue()).isEqualTo(expectedRoot.getValue());
    }
}