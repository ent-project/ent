package org.ent.net.node.cmd.veto;

import org.ent.util.TestUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class VetosTest {

    @ParameterizedTest
    @MethodSource("valueData")
    void getValue(String vetoName, int expectedValue) {
        Veto veto = Vetos.getByName(vetoName);
        assertThat(veto).isNotNull();
        assertThat(TestUtil.toBinary16bit(veto.getValue())).isEqualTo(TestUtil.toBinary16bit(expectedValue));
    }

    public static Stream<Arguments> valueData() {
        return Stream.of(
                arguments("?*==*?",   0b0100_0000__0000_0001__0001_0000__0000_0010),
                arguments("?===?",    0b0100_0000__0000_0011__0010_0000__0000_0000),
                arguments("?\\\\gt?", 0b0100_0000__0000_0011__0111_0000__0000_0100),
                arguments("?\\<=/?",  0b0100_0000__0000_0010__0011_0000__0000_0101),
                arguments("?!=/\\/?", 0b0100_0000__0000_1010__0010_0000__0000_0011)
        );
    }

}