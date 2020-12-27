package org.ent.net;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.ent.ExecutionEventListener;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.io.parser.NetParser;
import org.ent.net.node.BNode;
import org.ent.net.node.CNode;
import org.ent.net.node.Node;
import org.ent.net.node.UNode;
import org.ent.net.node.cmd.NopCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultNetControllerTest {

	@Mock
	private ExecutionEventListener eventListener;

	private NetParser parser = new NetParser();

	private UNode externalNode;

	@BeforeEach
	void setUp() throws Exception {
		parser = new NetParser();
		parser.parse("a=[a]");
		externalNode = (UNode) parser.getNodeNames().get("a");
	}

	@Test
	void getTarget() throws Exception {
		Net net = parser.parse("u=[_a=<nop>]");
		UNode u = (UNode) parser.getNodeNames().get("u");
		CNode nop = (CNode) parser.getNodeNames().get("_a");
		DefaultNetController controller = new DefaultNetController(net, eventListener);

		Node uTarget = controller.getTarget(u.getArrow());

		assertThat(uTarget).isSameAs(nop);
		verify(eventListener).fireGetChild(u, ArrowDirection.DOWN, null);
		verifyNoMoreInteractions(eventListener);
	}

	@Test
	void getTarget_error() {
		Net net = new Net();
		DefaultNetController controller = new DefaultNetController(net);

		assertThatThrownBy(() -> controller.getTarget(externalNode.getArrow()))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("arrow origin does not belong to controlled net");
	}

	@Test
	void setTarget() throws Exception {
		Net net = parser.parse("u=[<nop>]; _b=<ix>");
		UNode u = (UNode) parser.getNodeNames().get("u");
		CNode ix = (CNode) parser.getNodeNames().get("_b");
		DefaultNetController controller = new DefaultNetController(net, eventListener);

		controller.setTarget(u.getArrow(), ix);

		assertThat(u.getArrow().getTargetForNetControllerOnly()).isSameAs(ix);
		verify(eventListener).fireSetChild(u, ArrowDirection.DOWN, ix, null);
		verifyNoMoreInteractions(eventListener);
	}

	@Test
	void setTarget_error_rogueOrigin() throws Exception {
		Net net = parser.parse("_a=<nop>");
		CNode a = (CNode) parser.getNodeNames().get("_a");
		DefaultNetController controller = new DefaultNetController(net);

		assertThatThrownBy(() -> controller.setTarget(externalNode.getArrow(), a))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("arrow origin does not belong to controlled net");
	}

	@Test
	void setTarget_error_rogueTarget() throws Exception {
		Net net = parser.parse("u=[<nop>]");
		UNode u = (UNode) parser.getNodeNames().get("u");
		DefaultNetController controller = new DefaultNetController(net);

		assertThatThrownBy(() -> controller.setTarget(u.getArrow(), externalNode))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("target does not belong to controlled net");
	}

	@Test
	void newUNode() throws Exception {
		Net net = new Net();
		DefaultNetController controller = new DefaultNetController(net, eventListener);

		UNode uNode = controller.newUNode();

		assertThat(net.belongsToNet(uNode)).isTrue();
		assertThat(uNode.getArrow().getTargetForNetControllerOnly()).isEqualTo(uNode);
		verify(eventListener).fireNewNode(uNode);
		verifyNoMoreInteractions(eventListener);
	}

	@Test
	void newUNode_childArg() throws Exception {
		Net net = parser.parse("_a=<nop>");
		Node nop = parser.getNodeNames().get("_a");
		DefaultNetController controller = new DefaultNetController(net, eventListener);

		UNode uNode = controller.newUNode(nop);

		assertThat(net.belongsToNet(uNode)).isTrue();
		assertThat(uNode.getArrow().getTargetForNetControllerOnly()).isEqualTo(nop);
		verify(eventListener).fireNewNode(uNode);
		verifyNoMoreInteractions(eventListener);
	}

	@Test
	void newUNode_childArg_error() {
		Net net = new Net();
		DefaultNetController controller = new DefaultNetController(net);

		assertThatThrownBy(() -> controller.newUNode(externalNode))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("child does not belong to controlled net");
	}

	@Test
	void newBNode() {
		Net net = new Net();
		DefaultNetController controller = new DefaultNetController(net, eventListener);

		BNode bNode = controller.newBNode();

		assertThat(net.belongsToNet(bNode)).isTrue();
		assertThat(bNode.getLeftArrow().getTargetForNetControllerOnly()).isEqualTo(bNode);
		assertThat(bNode.getRightArrow().getTargetForNetControllerOnly()).isEqualTo(bNode);
		verify(eventListener).fireNewNode(bNode);
		verifyNoMoreInteractions(eventListener);
	}

	@Test
	void newBNode_childArgs() throws Exception {
		Net net = parser.parse("_a=<nop>; _b=<ix>");
		Node nop = parser.getNodeNames().get("_a");
		Node ix = parser.getNodeNames().get("_b");
		DefaultNetController controller = new DefaultNetController(net, eventListener);

		BNode bNode = controller.newBNode(nop, ix);

		assertThat(net.belongsToNet(bNode)).isTrue();
		assertThat(bNode.getLeftArrow().getTargetForNetControllerOnly()).isEqualTo(nop);
		assertThat(bNode.getRightArrow().getTargetForNetControllerOnly()).isEqualTo(ix);
		verify(eventListener).fireNewNode(bNode);
		verifyNoMoreInteractions(eventListener);
	}

	@Test
	void newBNode_childArgs_error_left() throws Exception {
		Net net = parser.parse("_a=<nop>");
		Node nop = parser.getNodeNames().get("_a");
		DefaultNetController controller = new DefaultNetController(net);

		assertThatThrownBy(() -> controller.newBNode(externalNode, nop))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("left child does not belong to controlled net");
	}

	@Test
	void newBNode_childArgs_error_right() throws Exception {
		Net net = parser.parse("_a=<nop>");
		Node nop = parser.getNodeNames().get("_a");
		DefaultNetController controller = new DefaultNetController(net);

		assertThatThrownBy(() -> controller.newBNode(nop, externalNode))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("right child does not belong to controlled net");
	}

	@Test
	void newCNode() {
		Net net = new Net();
		DefaultNetController controller = new DefaultNetController(net, eventListener);

		CNode cNode = controller.newCNode(new NopCommand());

		assertThat(net.belongsToNet(cNode)).isTrue();
		verify(eventListener).fireNewNode(cNode);
		verifyNoMoreInteractions(eventListener);
	}

	@Test
	void ancestorSwap() throws Exception {
		Net net = parser.parse("(([_a=<nop>], _b=<ix>), ((_a, _a), [_b]))");
		Node a = parser.getNodeNames().get("_a");
		Node b = parser.getNodeNames().get("_b");
		DefaultNetController controller = new DefaultNetController(net, eventListener);

		controller.ancestorSwap(a, b);

		NetFormatter formatter = new NetFormatter()
				.withNodeNamesInverse(parser.getNodeNames())
				.withAscii(true);
		assertThat(formatter.format(net)).isEqualTo("(([_b=<ix>], _a=<nop>), ((_b, _b), [_a]))");
		verifyNoMoreInteractions(eventListener);
	}

	@Test
	void ancestorSwap_error_first() throws Exception {
		Net net = parser.parse("_a=<nop>");
		Node nop = parser.getNodeNames().get("_a");
		DefaultNetController controller = new DefaultNetController(net);

		assertThatThrownBy(() -> controller.ancestorSwap(externalNode, nop))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("first node does not belong to controlled net");
	}

	@Test
	void ancestorSwap_error_second() throws Exception {
		Net net = parser.parse("_a=<nop>");
		Node nop = parser.getNodeNames().get("_a");
		DefaultNetController controller = new DefaultNetController(net);

		assertThatThrownBy(() -> controller.ancestorSwap(nop, externalNode))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("second node does not belong to controlled net");
	}

}
