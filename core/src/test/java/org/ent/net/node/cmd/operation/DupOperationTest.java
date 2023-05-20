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

class DupOperationTest {

    @ParameterizedTest
    @MethodSource("applyTestCases")
    void apply(String input, String expectedOutput) throws ParserException {
        NetParser parser = new NetParser();
        Net net = parser.parse(input);
        DupOperation dupOperation = new DupOperation();

        dupOperation.apply(net.getRoot().getLeftArrow(), net.getRoot().getRightArrow());

        NetFormatter formatter = new NetFormatter()
                .withForceGivenNodeNames(true);
        String result = formatter.format(net);
        assertThat(result).isEqualTo(expectedOutput);
    }

    public static Stream<Arguments> applyTestCases() {
        return Stream.of(arguments("(#1, (#2, #3))", "((_a:#2, _b:#3), (_a, _b)); #1"),
                arguments("(#1, #2)", "(#2(_a:#2, _a), _a); #1")
        );
    }
}