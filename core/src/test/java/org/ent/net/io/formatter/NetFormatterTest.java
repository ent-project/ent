package org.ent.net.io.formatter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.ent.net.DefaultNetController;
import org.ent.net.Net;
import org.ent.net.NetController;
import org.ent.net.NetTestData;
import org.ent.net.node.BNode;
import org.ent.net.node.CNode;
import org.ent.net.node.Node;
import org.ent.net.node.UNode;
import org.ent.net.node.cmd.NopCommand;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class NetFormatterTest {

	private static NetTestData testData;

	private NetFormatter formatter;

	@BeforeAll
	public static void setUpTestData() {
		testData = new NetTestData();
	}

	@BeforeEach
	public void setUp() {
		formatter = new NetFormatter();
		formatter.setAscii(true);
	}

	@ParameterizedTest(name = "{index} => format(...) should return {1}")
	@MethodSource("format_testData")
	public void format(Net net, String stringRepresentation) throws Exception {
		String str = formatter.format(net);

		assertThat(str).isEqualTo(stringRepresentation);
	}

	@SuppressWarnings("unused")
	private static Stream<Arguments> format_testData() {
		return testData.all.stream().map(nws -> Arguments.of(nws.getNet(), nws.getStringRepresentation()));
	}

	@Test
	public void format_2Calls() {
		Net net = new Net();
		NetController controller = new DefaultNetController(net);
		net.runWithMarkerNode(dummy -> {
			BNode b1 = controller.newBNode(dummy, dummy);
			UNode u1 = controller.newUNode(dummy);
			UNode u2 = controller.newUNode(dummy);

			b1.setLeftChild(controller, u1);
			b1.setRightChild(controller, u2);
			u1.setChild(controller, u1);
			u2.setChild(controller, u2);

			net.setRoot(b1);

			assertThat(formatter.format(net)).isEqualTo("(a=[a], b=[b])");

			b1.setLeftChild(controller, controller.newCNode(new NopCommand()));
			net.getNodes().remove(u1);

			assertThat(formatter.format(net)).isEqualTo("(<nop>, b=[b])");
		});
	}

	@Test
	public void format_3Calls() {
		String str0 = formatter.format(testData.net0.getNet());
		String str1 = formatter.format(testData.net1.getNet());
		String str2 = formatter.format(testData.net2.getNet());

		assertThat(str0).isEqualTo("(a=[a], <nop>)");
		assertThat(str1).isEqualTo("(b=[<nop>], (b, <nop>))");
		assertThat(str2).isEqualTo("c=[d=[(A=(<nop>, c), (A, (A, d)))]]");
	}

	@Test
	public void format_multipleRoots() {
		Net net = new Net();
		NetController controller = new DefaultNetController(net);
		net.runWithMarkerNode(dummy -> {
			BNode b1 = controller.newBNode(dummy, dummy);
			UNode u1 = controller.newUNode(dummy);
			CNode nop = controller.newCNode(new NopCommand());

			b1.setLeftChild(controller, u1);
			b1.setRightChild(controller, nop);
			u1.setChild(controller, u1);

			controller.newUNode(b1);
			net.setRoot(b1);
		});

		assertThat(formatter.format(net)).isEqualTo("A=(a=[a], <nop>); [A]");
	}

	@Test
	public void format_setNodeNames() {
		Net net = new Net();
		NetController controller = new DefaultNetController(net);

		UNode u1 = controller.newUNode();

		net.setRoot(u1);

		Map<Node, String> nodeNames = new HashMap<>();
		nodeNames.put(u1, "x1");
		formatter.setNodeNames(nodeNames);

		assertThat(formatter.format(net)).isEqualTo("x1=[x1]");
	}

	@Test
	public void format_maxDepth() {
		Net net = testData.buildNetDeep().getNet();
		formatter.setMaxDepth(3);

		assertThat(formatter.format(net)).isEqualTo("[[[...]]]");
	}
}
