package org.ent.net.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.ent.net.Net;
import org.ent.net.NetTestData;
import org.ent.net.node.Node;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ReferentialGarbageCollectionTest {

	@ParameterizedTest(name = "{index} => run()")
	@MethodSource("run_testData")
	public void run(Net net, Net netExtraNodes) throws Exception {
		Set<Node> originalNodes = new HashSet<>(net.getNodes());
		for (Node n : netExtraNodes.getNodes()) {
			net.addNode(n);
		}

		new ReferentialGarbageCollection(net).run();

		assertThat(net.getNodes()).isEqualTo(originalNodes);
	}

	private static Stream<Arguments> run_testData() {
		return Stream.of(
				of(NetTestData.buildNet0().getNet(), NetTestData.buildNet1().getNet()),
				of(NetTestData.buildNet2().getNet(), NetTestData.buildNet0().getNet()),
				of(NetTestData.buildNet1().getNet(), NetTestData.buildNet2().getNet())
		);
	}
}
