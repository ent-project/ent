package org.ent.net;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.ent.net.io.parser.NetParser;
import org.ent.net.node.BNode;
import org.ent.net.node.MarkerNode;
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
		assertThatThrownBy(() -> net.consistencyTest()).isInstanceOf(AssertionError.class)
				.hasMessage("Root is null");
	}

	@Test
	public void consistencyTest_error_rootNotInNet() throws Exception {
		Net net = new NetParser().parse("A=[A]");
		Net net2 = new NetParser().parse("x=(x,x)");
		net.setRoot(net2.getNodes().iterator().next());
		assertThatThrownBy(() -> net.consistencyTest()).isInstanceOf(AssertionError.class)
				.hasMessage("Root must be one of the net nodes");
	}

	@Test
	public void consistencyTest_error_rogueChild() throws Exception {
		Net net = new NetParser().parse("A=[A]");
		NetController controller = new DefaultNetController(net);
		Net net2 = new NetParser().parse("x=(x,x)");

		UNode root = (UNode) net.getRoot();
		root.setChild(controller, net2.getRoot());

		assertThatThrownBy(() -> net.consistencyTest()).isInstanceOf(AssertionError.class)
				.hasMessage("Child of node must be part of the net");
	}

	@Test
	public void consistencyTest_error_rogueParent() throws Exception {
		Net net = new NetParser().parse("A=[A]");
		Net net2 = new NetParser().parse("x=(x,x)");
		NetController controller2 = new DefaultNetController(net2);

		BNode root2 = (BNode) net2.getRoot();
		root2.setLeftChild(controller2, net.getRoot());

		assertThatThrownBy(() -> net.consistencyTest()).isInstanceOf(AssertionError.class)
				.hasMessage("Nodes referencing a net node must be part of the net");
	}

	@Test
	public void consistencyTest_error_markerInNet() throws Exception {
		Net net = new NetParser().parse("A=[A]");
		net.addNode(new MarkerNode());

		assertThatThrownBy(() -> net.consistencyTest()).isInstanceOf(AssertionError.class)
				.hasMessage("Net node must not be a marker node");
	}

	@Test
	public void consistencyTest_error_wrongMarker() throws Exception {
		NetParser parser = new NetParser();
		MarkerNode markerNode = new MarkerNode();
		parser.permitMarkerNodes(markerNode);
		Net net = parser.parse("[#]");
		MarkerNode markerNode2 = new MarkerNode();
		net.permitMarkerNode(markerNode2);

		assertThatThrownBy(() -> net.consistencyTest()).isInstanceOf(AssertionError.class)
				.hasMessage("Child of node is marker node, but not the designated one");
	}

	@Test
	public void addNodes() throws Exception {
		Net net = new NetParser().parse("A=[A]");
		assertThat(net.getNodes().size()).isEqualTo(1);
		Net net2 = new NetParser().parse("x=(x,x)");

		net.addNodes(net2.getNodes());

		assertThat(net.getNodes().size()).isEqualTo(2);
		assertThat(net.getNodes()).containsAll(net2.getNodes());
	}

}
