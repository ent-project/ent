package org.ent.net.node.cmd.split;

import org.ent.util.TestUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class SplitsTest {

    @ParameterizedTest
    @MethodSource("valueData")
    void getValue(String splitName, int expectedValue) {
        Split split = Splits.getByName(splitName);
        assertThat(split).isNotNull();
        assertThat(TestUtil.toBinary16bit(split.getValue())).isEqualTo(TestUtil.toBinary16bit(expectedValue));
    }

    public static Stream<Arguments> valueData() {
        return Stream.of(
                arguments("?/==/?",   0b0001_1010__0000_0001__0001_0000__0000_0010),
                arguments("?//===/\\?",    0b0001_1010__0000_0011__0010_0000__0000_0000),
                arguments("?/\\\\gt/\\?", 0b0001_1010__0000_0011__0111_0000__0000_0100),
                arguments("?/\\<=//?",  0b0001_1010__0000_0010__0011_0000__0000_0101),
                arguments("?//!=//\\/?", 0b0001_1010__0000_1010__0010_0000__0000_0011)
        );
    }

}