package org.ent.dev.plan;

import org.ent.net.node.CNode;
import org.ent.net.node.cmd.CommandFactory;
import org.ent.net.node.cmd.ExecutionResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class VariabilityEvaluatorTest {

    private static final CNode cNodeNop = new CNode(CommandFactory.createNopCommand());
    private static final CNode cNodeIx = new CNode(CommandFactory.createAncestorSwapCommand());

    @ParameterizedTest
    @MethodSource("getPoints")
    void getPoints(List<CNode> commandsExecuted, long pointsExpected) {
        VariabilityEvaluator evaluator = new VariabilityEvaluator();

        for (CNode cNode : commandsExecuted) {
            evaluator.fireCommandExecuted(cNode, ExecutionResult.NORMAL);
        }

        assertThat(evaluator.getPoints()).isEqualTo(pointsExpected);
    }

    private static Stream<Arguments> getPoints() {
        return Stream.of(
                arguments(List.of(), 0),
                arguments(List.of(cNodeNop), 1000),
                arguments(List.of(cNodeNop, cNodeIx), 2000),
                arguments(List.of(cNodeNop, cNodeNop), 1500),
                arguments(List.of(cNodeNop, cNodeNop, cNodeNop), 1750),
                arguments(List.of(cNodeNop, cNodeIx, cNodeNop), 2500),
                arguments(List.of(cNodeNop, cNodeIx, cNodeNop, cNodeIx), 3000)
        );
    }

    @ParameterizedTest
    @MethodSource("getDecayingPoints")
    void getDecayingPoints(int level, long expectedValue) {
        long actualValue = VariabilityEvaluator.getDecayingPoints(level);

        assertThat(actualValue).isEqualTo(expectedValue);
    }

    private static Stream<Arguments> getDecayingPoints() {
        return Stream.of(
                arguments(1, 1000),
                arguments(2, 500),
                arguments(3, 250),
                arguments(4, 125),
                arguments(5, 63),
                arguments(6, 31),
                arguments(7, 16),
                arguments(8, 8),
                arguments(9, 4),
                arguments(10, 2),
                arguments(11, 1),
                arguments(12, 0),
                arguments(13, 0),
                arguments(100, 0)
        );
    }

    @ParameterizedTest
    @MethodSource("getCumulativePointsDecaying")
    void getCumulativePointsDecaying(int level, long expectedValue) {
        long actualValue = VariabilityEvaluator.getCumulativePointsDecaying(level);

        assertThat(actualValue).isEqualTo(expectedValue);
    }

    private static Stream<Arguments> getCumulativePointsDecaying() {
        return Stream.of(
                arguments(0, 0),
                arguments(1, 1000),
                arguments(2, 1500),
                arguments(3, 1750),
                arguments(4, 1875),
                arguments(5, 1938),
                arguments(6, 1969),
                arguments(7, 1985),
                arguments(8, 1993),
                arguments(9, 1997),
                arguments(10, 1999),
                arguments(11, 2000),
                arguments(12, 2000),
                arguments(100, 2000)
        );
    }

}