package org.ent.net.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.ent.net.Net;
import org.ent.net.NetTestData;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.io.parser.NetParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NetCopyTest {

	private static NetTestData testData;

	@BeforeAll
	static void setUpTestData() {
		testData = new NetTestData();
	}

	@ParameterizedTest(name = "{index} => runCopy(...) on {1}")
	@MethodSource("netData")
	void createCopy(Net net, String str) {
		NetCopy copy = new NetCopy(net);

		copy.createCopy();

		Net clone = copy.getClonedNet();
		clone.consistencyCheck();
		assertThat(new NetFormatter().format(clone)).isEqualTo(new NetFormatter().format(net));
	}

	private static Stream<Arguments> netData() {
		return testData.all.stream().map(nws -> Arguments.of(nws.getNet(), nws.getStringRepresentation()));
	}

	@Test
	void createCopy_withMarker() throws Exception {
		Net net = new NetParser().permitMarkerNodes().parse("[#]");
		NetCopy copy = new NetCopy(net);

		copy.createCopy();

		Net clone = copy.getClonedNet();
		clone.consistencyCheck();
		assertThat(new NetFormatter().withAscii(true).format(clone)).isEqualTo("[#]");
		assertThat(clone.isMarkerNodePermitted()).isTrue();
		assertThat(clone.getMarkerNode()).isNotSameAs(net.getMarkerNode());
	}

}
