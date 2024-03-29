package org.ent.net.util;

import org.ent.Profile;
import org.ent.net.Net;
import org.ent.net.NetTestData;
import org.ent.net.node.Node;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

class NetUtilsTest {

	public static NetTestData data;

	@BeforeAll
	@Order(0)
	static void setTestEnvironment() {
		Profile.setTest(true);
	}

	@BeforeAll
	static void setUp() {
		data = new NetTestData();
	}

	@ParameterizedTest(name = "{index} => run()")
	@MethodSource("collectReachable_testData")
	void collectReachable(Net net) {
		Set<Node> nodes = NetUtils.collectReachable(net.getRoot());

		assertThat(nodes).containsExactlyInAnyOrderElementsOf(net.getNodes());
	}

	private static Stream<Arguments> collectReachable_testData() {
		return data.all.stream().map(netRep -> of(netRep.getNet()));
	}

}
