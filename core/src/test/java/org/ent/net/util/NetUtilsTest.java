package org.ent.net.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

import java.util.Set;
import java.util.stream.Stream;

import org.ent.net.Net;
import org.ent.net.NetTestData;
import org.ent.net.node.Node;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class NetUtilsTest {

	public static NetTestData data;

	@BeforeAll
	public static void setUp() {
		data = new NetTestData();
	}

	@ParameterizedTest(name = "{index} => run()")
	@MethodSource("collectReachable_testData")
	public void collectReachable(Net net) throws Exception {
		Set<Node> nodes = NetUtils.collectReachable(net.getRoot());

		assertThat(nodes).isEqualTo(net.getNodes());
	}

	private static Stream<Arguments> collectReachable_testData() {
		return data.all.stream().map(netRep -> of(netRep.getNet()));
	}

}
