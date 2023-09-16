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
    void getValue(String commandName, int expectedValue) {
        Command command = Commands.getByName(commandName);
        assertThat(command).isNotNull();
        assertThat(TestUtil.toBinary16bit(command.getValue())).isEqualTo(TestUtil.toBinary16bit(expectedValue));
    }

    public static Stream<Arguments> valueData() {
        return Stream.of(
                arguments("o", 0b0011_0011__0000_0000__0000_0000__0000_0000),
                arguments("\\::/", 0b0011_0011__0000_0001__0000_0000__0000_0001),
                arguments("/::/", 0b0011_0011__0000_0001__0001_0000__0000_0001),
                arguments("//::/\\", 0b0011_0011__0000_0011__0010_0000__0000_0001),
                arguments("//:://", 0b0011_0011__0000_0010__0010_0000__0000_0001),
                arguments("/\\\\::/\\", 0b0011_0011__0000_0011__0111_0000__0000_0001),
                arguments("/\\\\\\::/\\", 0b0011_0011__0000_0011__1111_0000__0000_0001),
                arguments("//x/\\/", 0b0011_0011__0000_0101__0010_0000__0000_0010)
        );
    }
}