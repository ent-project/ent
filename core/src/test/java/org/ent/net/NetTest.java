package org.ent.net;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.ent.net.io.parser.NetParser;
import org.ent.net.node.BNode;
import org.ent.net.node.CNode;
import org.ent.net.node.MarkerNode;
import org.ent.net.node.Node;
import org.ent.net.node.UNode;
import org.ent.net.node.cmd.NopCommand;
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
		Net net2 = new NetParser().parse("x=(x,x)");

		UNode root = (UNode) net.getRoot();
		root.getArrow().setTargetForNetControllerOnly(net2.getRoot());

		assertThatThrownBy(() -> net.consistencyTest()).isInstanceOf(AssertionError.class)
				.hasMessage("Child of node must be part of the net");
	}

	@Test
	public void consistencyTest_error_rogueParent() throws Exception {
		Net net = new NetParser().parse("A=[A]");
		Net net2 = new NetParser().parse("x=(x,x)");

		BNode root2 = (BNode) net2.getRoot();
		root2.getLeftArrow().setTargetForNetControllerOnly(net.getRoot());

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
	public void consistencyTest_error_markerNotPermitted() throws Exception {
		NetParser parser = new NetParser();
		parser.permitMarkerNodes(new MarkerNode());
		Net net = parser.parse("A=[#]");
		net.forbidMarkerNode();

		assertThatThrownBy(() -> net.consistencyTest()).isInstanceOf(AssertionError.class)
				.hasMessage("Child of node is marker node, but they are not permitted");
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

	@Test
	public void belongsToNet() throws Exception {
		NetParser parser = new NetParser();
		Net net = parser.parse("A=[A]");
		Node a = parser.getNodeNames().get("A");
		Node externalNode = new CNode(new NopCommand());

		assertThat(net.belongsToNet(a)).isTrue();
		assertThat(net.belongsToNet(externalNode)).isFalse();
	}

	@Test
	public void belongsToNet_marker() throws Exception {
		Net net = new Net();
		MarkerNode marker = new MarkerNode();
		net.permitMarkerNode(marker);
		Node externalMarker = new MarkerNode();

		assertThat(net.belongsToNet(marker)).isTrue();
		assertThat(net.belongsToNet(externalMarker)).isFalse();
	}

	@Test
	public void runWithMarkerNode() throws Exception {
		final Net net = new Net();
		assertThat(net.isMarkerNodePermitted()).isFalse();
		net.runWithMarkerNode(marker -> {
			assertThat(net.isMarkerNodePermitted()).isTrue();
			assertThat(net.getMarkerNode()).isSameAs(marker);
		});
		assertThat(net.isMarkerNodePermitted()).isFalse();
	}
}
