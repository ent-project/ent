package org.ent.net.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.ent.net.Net;
import org.ent.net.NetTestData;
import org.ent.net.io.formatter.NetFormatter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class NetCopyTest {

	private static NetTestData testData;

	@BeforeAll
	public static void setUpTestData() {
		testData = new NetTestData();
	}

	@ParameterizedTest(name = "{index} => runCopy(...) on {1}")
	@MethodSource("netData")
	public void runCopy(Net net, String str) throws Exception {
		NetCopy copy = new NetCopy(net);

		copy.runCopy();

		Net clone = copy.getClonedNet();
		clone.consistencyTest();
		assertThat(new NetFormatter().format(clone)).isEqualTo(new NetFormatter().format(net));
	}

	@SuppressWarnings("unused")
	private static Stream<Arguments> netData() {
		return testData.all.stream().map(nws -> Arguments.of(nws.getNet(), nws.getStringRepresentation()));
	}
}
