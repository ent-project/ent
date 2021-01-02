package org.ent.net;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.ent.ExecutionEventListener;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.io.parser.NetParser;
import org.ent.net.node.BNode;
import org.ent.net.node.CNode;
import org.ent.net.node.MarkerNode;
import org.ent.net.node.Node;
import org.ent.net.node.UNode;
import org.ent.net.node.cmd.CommandFactory;
import org.ent.net.node.cmd.NopCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class NetTest {

	NetParser parser = new NetParser();

	@Nested
	class Consistency {

		@Test
		void okay() throws Exception {
			Net net = parser.parse("A=[A]");
			assertThatCode(() -> net.consistencyCheck()).doesNotThrowAnyException();
		}

		@Nested
		class Errors {
			@Test
			void rootNull() throws Exception {
				Net net = parser.parse("A=[A]");
				net.setRoot(null);
				assertThatThrownBy(() -> net.consistencyCheck()).isInstanceOf(AssertionError.class)
						.hasMessage("Root is null");
			}

			@Test
			void rootNotInNet() throws Exception {
				Net net = parser.parse("A=[A]");
				Net net2 = parser.parse("x=(x,x)");
				net.setRoot(net2.getNodes().iterator().next());
				assertThatThrownBy(() -> net.consistencyCheck()).isInstanceOf(AssertionError.class)
						.hasMessage("Root must be one of the net nodes");
			}

			@Test
			void rogueChild() throws Exception {
				Net net = new NetParser().parse("A=[A]");
				Net net2 = new NetParser().parse("x=(x,x)");
				UNode root = (UNode) net.getRoot();
				MethodUtils.invokeMethod(root.getArrow(), true, "doSetTarget", net2.getRoot());

				assertThatThrownBy(() -> net.consistencyCheck()).isInstanceOf(AssertionError.class)
						.hasMessage("Child of node must be part of the net");
			}

			@Test
			void rogueParent() throws Exception {
				Net net = new NetParser().parse("A=[A]");
				Net net2 = new NetParser().parse("x=(x,x)");
				BNode root2 = (BNode) net2.getRoot();
				MethodUtils.invokeMethod(root2.getLeftArrow(), true, "doSetTarget", net.getRoot());

				assertThatThrownBy(() -> net.consistencyCheck()).isInstanceOf(AssertionError.class)
						.hasMessage("Nodes referencing a net node must be part of the net");
			}

			@Test
			void markerInNet() throws Exception {
				Net net = new NetParser().parse("A=[A]");
				net.addNode(new MarkerNode(null));

				assertThatThrownBy(() -> net.consistencyCheck()).isInstanceOf(AssertionError.class)
						.hasMessage("Net node must not be a marker node");
			}

			@Test
			void wrongMarker() throws Exception {
				Net net = new NetParser().permitMarkerNodes().parse("[#]");
				MarkerNode otherMarkerNode = new MarkerNode(net);
				FieldUtils.writeField(net, "markerNode", otherMarkerNode, true);

				assertThatThrownBy(() -> net.consistencyCheck()).isInstanceOf(AssertionError.class)
						.hasMessage("Child of node is marker node, but not the designated one");
			}

			@Test
			void markerNotPermitted() throws Exception {
				Net net = new NetParser().permitMarkerNodes().parse("A=[#]");
				net.forbidMarkerNode();

				assertThatThrownBy(() -> net.consistencyCheck()).isInstanceOf(AssertionError.class)
						.hasMessage("Child of node is marker node, but they are not permitted");
			}

			@Test
			void notOfNet() throws Exception {
				Net net = new NetParser().permitMarkerNodes().parse("A=[A]");
				Node root = net.getRoot();
				root.setNet(new Net());

				assertThatThrownBy(() -> net.consistencyCheck()).isInstanceOf(AssertionError.class)
						.hasMessage("Node belongs to another net");
			}

			@Test
			void noInverseReference() throws Exception {
				NetParser parser = new NetParser().permitMarkerNodes();
				Net net = parser.parse("u=[c=<nop>]");
				Node uNode = parser.getNodeNames().get("u");
				Node cNode = parser.getNodeNames().get("c");
				cNode.getHub().removeInverseReference(uNode.getArrow(ArrowDirection.DOWN));

				assertThatThrownBy(() -> net.consistencyCheck()).isInstanceOf(AssertionError.class)
						.hasMessage("Child nodes must be aware of their parents");
			}

		}
	}

	@Test
	void addNodes_okay() throws Exception {
		Net net = new NetParser().parse("A=[A]");
		assertThat(net.getNodes().size()).isEqualTo(1);
		Net net2 = new NetParser().parse("x=(x,x)");

		net.addNodes(net2.removeAllNodes());

		assertThat(net.getNodes().size()).isEqualTo(2);
		assertThat(net.getNodes()).containsAll(net2.getNodes());
	}

	@Test
	void addNodes_error() throws Exception {
		Net net = parser.parse("A=[A]");
		assertThat(net.getNodes().size()).isEqualTo(1);
		Net net2 = parser.parse("x=(x,x)");

		Set<Node> net2Nodes = net2.getNodes();

		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> net.addNodes(net2Nodes))
				.withMessage("net in node must be unset");
	}

	@Test
	void validateBelongsToNet_okay() throws Exception {
		Net net = parser.parse("A=[A]");
		Node a = parser.getNodeNames().get("A");

		assertThatCode(() -> net.validateBelongsToNet(a)).doesNotThrowAnyException();
	}

	@Test
	void validateBelongsToNet_netDoesNotKnowTheNode_error() {
		Net net = new Net();
		Node node = net.newCNode(CommandFactory.NOP_COMMAND);
		net.removeNode(node);

		assertThatExceptionOfType(IllegalStateException.class)
				.isThrownBy(() -> net.validateBelongsToNet(node))
				.withMessage("node does not belong to this net");
	}

	@Test
	void validateBelongsToNet_nodeHasDifferentParentNet_error() {
		Net net = new Net();
		Node node = net.newCNode(CommandFactory.NOP_COMMAND);
		Net otherNet = new Net();
		node.setNet(otherNet);

		assertThatExceptionOfType(IllegalStateException.class)
				.isThrownBy(() -> net.validateBelongsToNet(node))
				.withMessage("node belongs to another net");
	}

	@Test
	void validateBelongsToNet_marker_okay() {
		Net net = new Net();
		MarkerNode marker = net.permitMarkerNode();

		assertThatCode(() -> net.validateBelongsToNet(marker)).doesNotThrowAnyException();
	}

	@Test
	void validateBelongsToNet_marker_shouldThrowErrorForExternal() {
		Net net = new Net();
		Net otherNet = new Net();
		MarkerNode externalMarker = otherNet.permitMarkerNode();

		assertThatExceptionOfType(IllegalStateException.class)
				.isThrownBy(() -> net.validateBelongsToNet(externalMarker));
	}

	@Test
	void runWithMarkerNode() {
		final Net net = new Net();
		net.setRoot(net.newUNode());

		assertThat(net.isMarkerNodePermitted()).isFalse();
		net.runWithMarkerNode(marker -> {
			assertThat(net.isMarkerNodePermitted()).isTrue();
			assertThat(net.getMarkerNode()).isSameAs(marker);
		});
		assertThat(net.isMarkerNodePermitted()).isFalse();
	}

	@Nested
	class ControllerFeatures {

		@Mock
		private ExecutionEventListener eventListener;

		private UNode externalNode;

		private Arrow externalArrow;

		@BeforeEach
		void setUp() throws Exception {
			parser.parse("a=[a]");
			externalNode = (UNode) parser.getNodeNames().get("a");
			externalArrow = externalNode.getArrow();
		}

		@Test
		void getTarget() throws Exception {
			Net net = parser.parse("u=[_a=<nop>]");
			UNode u = (UNode) parser.getNodeNames().get("u");
			CNode nop = (CNode) parser.getNodeNames().get("_a");
			net.addExecutionEventListener(eventListener);

			Node uTarget = u.getChild(Manner.DIRECT);

			assertThat(uTarget).isSameAs(nop);
			verify(eventListener).fireGetChild(u, ArrowDirection.DOWN, Manner.DIRECT);
			verifyNoMoreInteractions(eventListener);
		}

		@Test
		void setTarget() throws Exception {
			Net net = parser.parse("u=[<nop>]; _b=<ix>");
			UNode u = (UNode) parser.getNodeNames().get("u");
			CNode ix = (CNode) parser.getNodeNames().get("_b");
			net.addExecutionEventListener(eventListener);

			u.setChild(ix, Manner.DIRECT);

			verify(eventListener).fireSetChild(u, ArrowDirection.DOWN, ix, Manner.DIRECT);
			verifyNoMoreInteractions(eventListener);
			assertThat(u.getChild(Manner.DIRECT)).isSameAs(ix);
		}

		@Test
		void setTarget_error_rogueOrigin() throws Exception {
			parser.parse("_a=<nop>");
			CNode a = (CNode) parser.getNodeNames().get("_a");

			assertThatThrownBy(() -> externalArrow.setTarget(a, Manner.DIRECT))
					.isInstanceOf(IllegalStateException.class)
					.hasMessage("node belongs to another net");
		}

		@Test
		void setTarget_error_rogueTarget() throws Exception {
			parser.parse("u=[<nop>]");
			UNode u = (UNode) parser.getNodeNames().get("u");
			Arrow uArrow = u.getArrow();

			assertThatThrownBy(() -> uArrow.setTarget(externalNode, Manner.DIRECT))
					.isInstanceOf(IllegalStateException.class)
					.hasMessage("node belongs to another net");
		}

		@Test
		void newUNode() {
			Net net = new Net();
			net.addExecutionEventListener(eventListener);

			UNode uNode = net.newUNode();

			net.validateBelongsToNet(uNode);
			verify(eventListener).fireNewNode(uNode);
			verifyNoMoreInteractions(eventListener);
			assertThat(uNode.getChild(Manner.DIRECT)).isEqualTo(uNode);
		}

		@Test
		void newUNode_childArg() throws Exception {
			Net net = parser.parse("_a=<nop>");
			Node nop = parser.getNodeNames().get("_a");
			net.addExecutionEventListener(eventListener);

			UNode uNode = net.newUNode(nop);

			net.validateBelongsToNet(uNode);
			verify(eventListener).fireNewNode(uNode);
			verifyNoMoreInteractions(eventListener);
			assertThat(uNode.getChild(Manner.DIRECT)).isEqualTo(nop);
		}

		@Test
		void newBNode() {
			Net net = new Net();
			net.addExecutionEventListener(eventListener);

			BNode bNode = net.newBNode();

			net.validateBelongsToNet(bNode);
			verify(eventListener).fireNewNode(bNode);
			verifyNoMoreInteractions(eventListener);
			assertThat(bNode.getLeftChild(Manner.DIRECT)).isEqualTo(bNode);
			assertThat(bNode.getRightChild(Manner.DIRECT)).isEqualTo(bNode);
		}

		@Test
		void newBNode_childArgs() throws Exception {
			Net net = parser.parse("_a=<nop>; _b=<ix>");
			Node nop = parser.getNodeNames().get("_a");
			Node ix = parser.getNodeNames().get("_b");
			net.addExecutionEventListener(eventListener);

			BNode bNode = net.newBNode(nop, ix);

			net.validateBelongsToNet(bNode);
			verify(eventListener).fireNewNode(bNode);
			verifyNoMoreInteractions(eventListener);
			assertThat(bNode.getLeftChild(Manner.DIRECT)).isEqualTo(nop);
			assertThat(bNode.getRightChild(Manner.DIRECT)).isEqualTo(ix);
		}

		@Test
		void newBNode_childArgs_error_left() throws Exception {
			Net net = parser.parse("_a=<nop>");
			Node nop = parser.getNodeNames().get("_a");

			assertThatThrownBy(() -> net.newBNode(externalNode, nop))
					.isInstanceOf(IllegalStateException.class)
					.hasMessage("node belongs to another net");
		}

		@Test
		void newBNode_childArgs_error_right() throws Exception {
			Net net = parser.parse("_a=<nop>");
			Node nop = parser.getNodeNames().get("_a");

			assertThatThrownBy(() -> net.newBNode(nop, externalNode))
					.isInstanceOf(IllegalStateException.class)
					.hasMessage("node belongs to another net");
		}

		@Test
		void newCNode() {
			Net net = new Net();
			net.addExecutionEventListener(eventListener);

			CNode cNode = net.newCNode(new NopCommand());

			net.validateBelongsToNet(cNode);
			verify(eventListener).fireNewNode(cNode);
			verifyNoMoreInteractions(eventListener);
		}

		@Test
		void ancestorSwap() throws Exception {
			Net net = parser.parse("(([_a=<nop>], _b=<ix>), ((_a, _a), [_b]))");
			Node a = parser.getNodeNames().get("_a");
			Node b = parser.getNodeNames().get("_b");
			net.addExecutionEventListener(eventListener);

			Net.ancestorSwap(a, b);

			verifyNoMoreInteractions(eventListener);
			NetFormatter formatter = new NetFormatter()
					.withNodeNamesInverse(parser.getNodeNames())
					.withAscii(true);
			assertThat(formatter.format(net)).isEqualTo("(([_b=<ix>], _a=<nop>), ((_b, _b), [_a]))");
		}

		@Test
		void ancestorSwap_error_first() throws Exception {
			parser.parse("_a=<nop>");
			Node nop = parser.getNodeNames().get("_a");

			assertThatThrownBy(() -> Net.ancestorSwap(externalNode, nop))
					.isInstanceOf(IllegalStateException.class)
					.hasMessage("node belongs to another net");
		}

		@Test
		void ancestorSwap_error_second() throws Exception {
			parser.parse("_a=<nop>");
			Node nop = parser.getNodeNames().get("_a");

			assertThatThrownBy(() -> Net.ancestorSwap(nop, externalNode))
					.isInstanceOf(IllegalStateException.class)
					.hasMessage("node belongs to another net");
		}
	}
}
