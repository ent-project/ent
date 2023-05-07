package org.ent.net.node.cmd;

import org.ent.util.TestUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class CommandFactoryTest {

    @ParameterizedTest
    @MethodSource("valueData")
    void getValue(String commandName, String expectedValuePart1, String expectedValuePart2, String expectedValuePart3) {
        Command command = CommandFactory.getByName(commandName);
        assertThat(command).isNotNull();
        String expectedValue = expectedValuePart1 + expectedValuePart2 + expectedValuePart3;
        assertThat(TestUtil.toBinary16bit(command.getValue())).isEqualTo(expectedValue);
    }

    public static Stream<Arguments> valueData() {
        return Stream.of(
                arguments("o", "0000", "0000", "00000000"),
                arguments("=", "0001", "0001", "00000001"),
                arguments("/=/", "0010", "0010", "00000001"),
                arguments("\\\\=", "0001", "0111", "00000001"),
                arguments("\\\\\\=", "0001", "1111", "00000001"),
                arguments("x\\/", "0101", "0001", "00000010")
        );
    }
}