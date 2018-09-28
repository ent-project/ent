package org.ent.net;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.assertj.core.api.Assertions;
import org.ent.net.io.parser.NetParser;
import org.ent.net.node.BNode;
import org.ent.net.node.UNode;
import org.junit.jupiter.api.Test;

public class NetTest {

	@Test
	public void consistencyTest_okay() throws Exception {
		Net net = new NetParser().parse("A=[A]");
		assertThatCode(() -> net.consistencyTest()).doesNotThrowAnyException();
	}

	@Test
	public void consistencyTest_error_rootNull() throws Exception {
		Net net = new NetParser().parse("A=[A]");
		net.setRoot(null);
		Assertions.assertThatThrownBy(() -> net.consistencyTest()).isInstanceOf(AssertionError.class)
				.hasMessage("Root is null");
	}

	@Test
	public void consistencyTest_error_rootNotInNet() throws Exception {
		Net net = new NetParser().parse("A=[A]");
		Net net2 = new NetParser().parse("x=(x,x)");
		net.setRoot(net2.getNodes().iterator().next());
		Assertions.assertThatThrownBy(() -> net.consistencyTest()).isInstanceOf(AssertionError.class)
		.hasMessage("Root must be one of the net nodes");
	}

	@Test
	public void consistencyTest_error_rogueChild() throws Exception {
		Net net = new NetParser().parse("A=[A]");
		NetController controller = new DefaultNetController(net);
		Net net2 = new NetParser().parse("x=(x,x)");

		UNode root = (UNode) net.getRoot();
		root.setChild(controller, net2.getRoot());

		Assertions.assertThatThrownBy(() -> net.consistencyTest()).isInstanceOf(AssertionError.class)
		.hasMessage("Child of node must be part of the net");
	}

	@Test
	public void consistencyTest_error_rogueParent() throws Exception {
		Net net = new NetParser().parse("A=[A]");
		Net net2 = new NetParser().parse("x=(x,x)");
		NetController controller2 = new DefaultNetController(net2);

		BNode root2 = (BNode) net2.getRoot();
		root2.setLeftChild(controller2, net.getRoot());

		Assertions.assertThatThrownBy(() -> net.consistencyTest()).isInstanceOf(AssertionError.class)
		.hasMessage("Nodes referencing a net node must be part of the net");
	}

}
