package org.ent.net.node.cmd.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class EvalOperationTest {

	@ParameterizedTest
	@MethodSource
	void toSuperscriptNumber(int input, String expected) {
		String actual = EvalOperation.toSuperscriptNumber(input);

		assertThat(actual).isEqualTo(expected);
	}

	private static Stream<Arguments> toSuperscriptNumber() {
		return Stream.of(
				arguments(0, "⁰"),
				arguments(1, "¹"),
				arguments(9, "⁹"),
				arguments(26035, "²⁶⁰³⁵")
		);
	}
}
