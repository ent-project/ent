package org.ent.net.node.cmd.operation;

import org.ent.Ent;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.io.parser.NetParser;
import org.ent.net.io.parser.ParserException;
import org.ent.net.node.Node;
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
        Ent ent = parser.parseEnt(input);
        AncestorExchangeNormalOperation ancestorExchangeNormalOperation = new AncestorExchangeNormalOperation();

        Node root = ent.getNet().getRoot();
        ancestorExchangeNormalOperation.apply(root.getLeftArrow(), root.getRightArrow(), ent);

        NetFormatter formatter = new NetFormatter()
                .withForceGivenNodeNames(true);
        String result = formatter.format(ent);
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