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

class AncestorExchangeNormalOperationTest {

    @ParameterizedTest
    @MethodSource("applyTestCases")
    void apply(String input, String expectedOutput) throws ParserException {
        NetParser parser = new NetParser();
        Net net = parser.parse(input);
        AncestorExchangeNormalOperation ancestorExchangeNormalOperation = new AncestorExchangeNormalOperation();

        ancestorExchangeNormalOperation.apply(net.getRoot().getLeftArrow(), net.getRoot().getRightArrow());

        NetFormatter formatter = new NetFormatter()
                .withAscii(true)
                .withForceGivenNodeNames(true);
        String result = formatter.format(net);
        assertThat(result).isEqualTo(expectedOutput);
    }

    public static Stream<Arguments> applyTestCases() {
        return Stream.of(arguments("(#1, #2)", "(#2, #1)"),
                arguments("([#1], #2)", "(#2, [#1])"),
                arguments("(a:[[a]], b:#1)", "(b:#1, a:[[b]])"),
                arguments("((#1, #2), (#3, #4))", "((#3, #4), (#1, #2))")
        );
    }
}