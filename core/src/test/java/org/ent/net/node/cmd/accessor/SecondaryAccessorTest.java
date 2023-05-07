package org.ent.net.node.cmd.accessor;

import org.assertj.core.api.Assertions;
import org.ent.net.ArrowDirection;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class SecondaryAccessorTest {

    @ParameterizedTest
    @MethodSource("codeData")
    void code(ArrowDirection direction1, ArrowDirection direction2, int expectedCode) {
        SecondaryAccessor secondaryAccessor = new SecondaryAccessor(direction1, direction2);
        Assertions.assertThat(secondaryAccessor.getCode()).isEqualTo(expectedCode);
    }

    public static Stream<Arguments> codeData() {
        return Stream.of(
                arguments(ArrowDirection.LEFT, ArrowDirection.LEFT, 0b100),
                arguments(ArrowDirection.LEFT, ArrowDirection.RIGHT, 0b110),
                arguments(ArrowDirection.RIGHT, ArrowDirection.LEFT, 0b101),
                arguments(ArrowDirection.RIGHT, ArrowDirection.RIGHT, 0b111)
        );
    }
}