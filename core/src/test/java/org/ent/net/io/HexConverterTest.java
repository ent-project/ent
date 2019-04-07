package org.ent.net.io;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class HexConverterTest {

	@ParameterizedTest(name = "{index} => run()")
	@MethodSource("hexConverter_testData")
	public void toHex(long value, String hexExpected) throws Exception {
		String hexActual = HexConverter.toHex(value);

		assertThat(hexActual).isEqualTo(hexExpected);
	}

	@ParameterizedTest(name = "{index} => run()")
	@MethodSource("hexConverter_testData")
	public void fromHex(long valueExpected, String hexString) throws Exception {
		long valueActual = HexConverter.fromHex(hexString);

		assertThat(valueActual).isEqualTo(valueExpected);
	}

	@SuppressWarnings("unused")
	private static Stream<Arguments> hexConverter_testData() {
		return Stream.of(
				of(2772360572640341275L,  	"267967409186ad1b"),
				of(1104248111836207787L, 	"0f5313cea1e49aab"),
				of(-5644468487492645556L, 	"b1aad1d0f91d4d4c"),
				of(120L, 					"0000000000000078"),
				of(-1L, 					"ffffffffffffffff"),
				of(0L, 						"0000000000000000")
		);
	}
}
