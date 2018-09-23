package org.ent.net.io.formatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

import java.util.stream.Stream;

import org.ent.net.io.formatter.VariableNameHelper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class VariableNameHelperTest {

	@ParameterizedTest(name = "{index} => getLetterBasedVariableNameForIndex({0}) should return {1}")
	@MethodSource("getLetterBasedVariableNameForIndex_testData")
	public void getLetterBasedVariableNameForIndex(int index, String variableName) throws Exception {
		assertThat(VariableNameHelper.getLetterBasedVariableNameForIndex(index)).isEqualTo(variableName);
	}

	@SuppressWarnings("unused")
	private static Stream<Arguments> getLetterBasedVariableNameForIndex_testData() {
	    return Stream.of(
			of(0, "a"),
			of(1, "b"),
			of(2, "c"),
			of(25, "z"),
			of(26, "aa"),
			of(27, "ab"),
			of(51, "az"),
			of(52, "ba"),
			of(53, "bb"),
			of(77, "bz"),
			of(701, "zz"),
			of(702, "aaa"),
			of(703, "aab"),
			of(26 + 26*26 + 26*26*26 - 1, "zzz"),
			of(26 + 26*26 + 26*26*26, "aaaa")
	    );
	}

}
