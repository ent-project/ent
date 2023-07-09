package org.ent.net.util;

import org.ent.Profile;
import org.ent.net.Net;
import org.ent.net.NetTestData;
import org.ent.net.node.Node;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

class ReferentialGarbageCollectionTest {

	@BeforeAll
	static void setTestEnvironment() {
		Profile.setTest(true);
	}

	@ParameterizedTest(name = "{index} => run()")
	@MethodSource("run_testData")
	void run(Net net, Net netExtraNodes) {
		List<Node> originalNodes = new ArrayList<>(net.getNodes());
		for (Node n : netExtraNodes.removeAllNodes()) {
			net.addNode(n);
		}

		new ReferentialGarbageCollection(net).run();

		assertThat(net.getNodes().stream().filter(Objects::nonNull).toList()).isEqualTo(originalNodes);
	}

	private static Stream<Arguments> run_testData() {
		return Stream.of(
				of(NetTestData.buildNet0().getNet(), NetTestData.buildNet1().getNet()),
				of(NetTestData.buildNet2().getNet(), NetTestData.buildNet0().getNet()),
				of(NetTestData.buildNet1().getNet(), NetTestData.buildNet2().getNet())
		);
	}
}
