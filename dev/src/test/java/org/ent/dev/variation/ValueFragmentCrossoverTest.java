package org.ent.dev.variation;

import org.ent.Profile;
import org.ent.net.Net;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.accessor.Accessors;
import org.ent.net.node.cmd.operation.Operations;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ValueFragmentCrossoverTest {
    @BeforeAll
    static void setTestEnvironment() {
        Profile.setTest(true);
    }

    public static Stream<Arguments> swapValuesData() {

        int inputValue1 = Commands.get(Operations.MODULO_OPERATION, Accessors.LEFT_LEFT_RIGHT, Accessors.FLOW, Accessors.RIGHT_RIGHT).getValue();
        int inputValue2 = Commands.get(Operations.MULTIPLY_OPERATION, Accessors.DIRECT, Accessors.RIGHT_LEFT, Accessors.LEFT).getValue();
        return Stream.of(arguments(
                        inputValue1, inputValue2, 0,
                        Commands.get(Operations.MULTIPLY_OPERATION, Accessors.LEFT_LEFT_RIGHT, Accessors.FLOW, Accessors.RIGHT_RIGHT).getValue(),
                        Commands.get(Operations.MODULO_OPERATION, Accessors.DIRECT, Accessors.RIGHT_LEFT, Accessors.LEFT).getValue()
                ),
                arguments(
                        inputValue1, inputValue2, 1,
                        Commands.get(Operations.MODULO_OPERATION, Accessors.DIRECT, Accessors.FLOW, Accessors.RIGHT_RIGHT).getValue(),
                        Commands.get(Operations.MULTIPLY_OPERATION, Accessors.LEFT_LEFT_RIGHT, Accessors.RIGHT_LEFT, Accessors.LEFT).getValue()
                ),
                arguments(
                        inputValue1, inputValue2, 2,
                        Commands.get(Operations.MODULO_OPERATION, Accessors.LEFT_LEFT_RIGHT, Accessors.RIGHT_LEFT, Accessors.RIGHT_RIGHT).getValue(),
                        Commands.get(Operations.MULTIPLY_OPERATION, Accessors.DIRECT, Accessors.FLOW, Accessors.LEFT).getValue()
                ),
                arguments(
                        inputValue1, inputValue2, 3,
                        Commands.get(Operations.MODULO_OPERATION, Accessors.LEFT_LEFT_RIGHT, Accessors.FLOW, Accessors.LEFT).getValue(),
                        Commands.get(Operations.MULTIPLY_OPERATION, Accessors.DIRECT, Accessors.RIGHT_LEFT, Accessors.RIGHT_RIGHT).getValue()
                )
        );
    }

    @ParameterizedTest
    @MethodSource("swapValuesData")
    void swapValues(int inputValue1, int inputValue2, int maskIndex, int expectedOutputValue1, int expectedOutputValue2) {
        Net net = new Net();
        Node node1 = net.newCNode(inputValue1);
        Node node2 = net.newCNode(inputValue2);
        ValueFragmentCrossover.swapValue(node1, node2, maskIndex);
        assertThat(Integer.toBinaryString(node1.getValue())).isEqualTo(Integer.toBinaryString(expectedOutputValue1));
        assertThat(Integer.toBinaryString(node2.getValue())).isEqualTo(Integer.toBinaryString(expectedOutputValue2));
    }
}