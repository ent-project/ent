package org.ent.net.node.cmd.operation;

import org.ent.net.Net;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.io.parser.NetParser;
import org.ent.net.io.parser.ParserException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class DupNormalOperationTest {

    @ParameterizedTest
    @MethodSource("applyTestCases")
    void apply(String input, String expectedOutput) throws ParserException {
        NetParser parser = new NetParser();
        Net net = parser.parse(input);
        DupNormalOperation dupNormalOperation = new DupNormalOperation();

        dupNormalOperation.apply(net.getRoot().getLeftArrow(), net.getRoot().getRightArrow());

        NetFormatter formatter = new NetFormatter()
                .withAscii(true)
                .withNodeNamesInverse(parser.getNodeNames())
                .withForceGivenNodeNames(true);
        String result = formatter.format(net);
        assertThat(result).isEqualTo(expectedOutput);
    }

    public static Stream<Arguments> applyTestCases() {
        return Stream.of(arguments("(x:#1, y:#2)", "(#2, y:#2); x:#1"),
                arguments("(A:(#1, #2), B:(#3, #4))", "((_a:#3, _b:#4), B:(_a, _b)); A:(#1, #2)")
        );
    }
}