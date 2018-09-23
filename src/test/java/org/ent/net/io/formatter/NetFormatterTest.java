package org.ent.net.io.formatter;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.ent.net.DefaultNetController;
import org.ent.net.Net;
import org.ent.net.NetController;
import org.ent.net.NetTestData;
import org.ent.net.node.BNode;
import org.ent.net.node.CNode;
import org.ent.net.node.MarkerNode;
import org.ent.net.node.Node;
import org.ent.net.node.UNode;
import org.ent.net.node.cmd.NopCommand;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class NetFormatterTest {

	private static NetTestData testData;

	@BeforeAll
	public static void setup() {
		testData = new NetTestData();
	}

	@ParameterizedTest(name = "{index} => format(...) should return {1}")
	@MethodSource("format_testData")
	public void format(Net net, String stringRepresentation) throws Exception {
		NetFormatter formatter = new NetFormatter();
		Assertions.assertThat(formatter.format(net)).isEqualTo(stringRepresentation);
	}

	@SuppressWarnings("unused")
	private static Stream<Arguments> format_testData() {
		return testData.all.stream().map(nws -> Arguments.of(nws.getNet(), nws.getStringRepresentation()));
	}

	@Test
	public void format_multipleCalls() {
		Net net = new Net();
    	NetController controller = new DefaultNetController(net);
        Node dummy = new MarkerNode();

        BNode b1 = controller.newBNode(dummy, dummy);
        UNode u1 = controller.newUNode(dummy);
        UNode u2 = controller.newUNode(dummy);

        b1.setLeftChild(controller, u1);
        b1.setRightChild(controller, u2);
        u1.setChild(controller, u1);
        u2.setChild(controller, u2);

        net.setRoot(b1);

        NetFormatter formatter = new NetFormatter();
        Assertions.assertThat(formatter.format(net)).isEqualTo("(a=[a], b=[b])");

        b1.setLeftChild(controller, controller.newCNode(new NopCommand()));
        net.getNodes().remove(u1);
        Assertions.assertThat(formatter.format(net)).isEqualTo("(<nop>, b=[b])");
	}

	@Test
	public void format_multipleRoots() {
    	Net net = new Net();
    	NetController controller = new DefaultNetController(net);
        Node dummy = new MarkerNode();

        BNode b1 = controller.newBNode(dummy, dummy);
        UNode u1 = controller.newUNode(dummy);
        CNode nop = controller.newCNode(new NopCommand());

        b1.setLeftChild(controller, u1);
        b1.setRightChild(controller, nop);
        u1.setChild(controller, u1);

    	controller.newUNode(b1);

    	net.setRoot(b1);

        NetFormatter formatter = new NetFormatter();
        Assertions.assertThat(formatter.format(net)).isEqualTo("A=(a=[a], <nop>); [A]");
	}

	@Test
	public void format_setNodeNames() {
		Net net = new Net();
    	NetController controller = new DefaultNetController(net);
        Node dummy = new MarkerNode();

        UNode u1 = controller.newUNode(dummy);
        u1.setChild(controller, u1);

        net.setRoot(u1);

        NetFormatter formatter = new NetFormatter();
        Map<Node, String> nodeNames = new HashMap<>();
        nodeNames.put(u1, "x1");
		formatter.setNodeNames(nodeNames);
        Assertions.assertThat(formatter.format(net)).isEqualTo("x1=[x1]");
	}

	@Test
	public void format_maxDepth() {
		Net net = testData.buildNetDeep().getNet();
		NetFormatter formatter = new NetFormatter();
		formatter.setMaxDepth(3);

        Assertions.assertThat(formatter.format(net)).isEqualTo("[[[...]]]");
	}
}
