package org.ent.net;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.ent.Profile;
import org.ent.listener.NetEventListener;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.io.parser.NetParser;
import org.ent.net.node.MarkerNode;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.NopCommand;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.ent.util.NetBuilder.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class NetTest {

    NetParser parser = new NetParser();

    @BeforeAll
    static void setTestEnvironment() {
        Profile.setTest(true);
    }

    @Nested
    class Consistency {

        @Test
        void okay() throws Exception {
            Net net = parser.parse("[#1]");
            assertThatCode(net::consistencyCheck).doesNotThrowAnyException();
        }

        @Nested
        class Errors {
            @Test
            void rootNotInNet() throws Exception {
                Net net = parser.parse("[#1]");
                Net net2 = parser.parse("(<o>,<//x/\\>)");
                assertThatThrownBy(() -> net.setRoot(net2.getNodes().iterator().next()))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessage("Root must be one of the net nodes");
            }

            @Test
            void markerInNet() throws Exception {
                Net net = new NetParser().parse("[#1]");
                net.addNode(new MarkerNode(null));

                assertThatThrownBy(net::consistencyCheck).isInstanceOf(AssertionError.class)
                        .hasMessage("Net node must not be a marker node");
            }

            @Test
            void wrongMarker() throws Exception {
                Net net = new NetParser().permitMarkerNodes().parse("[@]");
                MarkerNode otherMarkerNode = new MarkerNode(net);
                FieldUtils.writeField(net, "markerNode", otherMarkerNode, true);

                assertThatThrownBy(net::consistencyCheck).isInstanceOf(AssertionError.class)
                        .hasMessage("Child of node is marker node, but not the designated one");
            }

            @Test
            void markerNotPermitted() throws Exception {
                Net net = new NetParser().permitMarkerNodes().parse("[@]");
                net.forbidMarkerNode();

                assertThatThrownBy(net::consistencyCheck).isInstanceOf(AssertionError.class)
                        .hasMessage("Child of node is marker node, but they are not permitted");
            }

            @Test
            void notOfNet() throws Exception {
                Net net = new NetParser().permitMarkerNodes().parse("[#1]");
                Node root = net.getRoot();
                root.setNet(new Net());

                assertThatThrownBy(net::consistencyCheck).isInstanceOf(AssertionError.class)
                        .hasMessage("Node belongs to another net");
            }
        }
    }

    @Test
    void addNodes_okay() throws Exception {
        Net net = new NetParser().parse("<o>");
        assertThat(net.getNodes()).hasSize(1);
        Net net2 = new NetParser().parse("<//x/\\>");

        net.addNodes(net2.removeAllNodes());

        assertThat(net.getNodes()).hasSize(2);
        assertThat(net.getNodes()).containsAll(net2.getNodes());
    }

    @Test
    void addNodes_error() throws Exception {
        Net net = parser.parse("<o>");
        assertThat(net.getNodes()).hasSize(1);
        Net net2 = parser.parse("<//x/\\>");

        List<Node> net2Nodes = net2.getNodes();

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> net.addNodes(net2Nodes))
                .withMessage("net in node must be unset");
    }

    @Test
    void validateBelongsToNet_okay() throws Exception {
        Net net = parser.parse("a:<o>");
        Node a = net.getByName("a");

        assertThatCode(() -> net.validateBelongsToNet(a)).doesNotThrowAnyException();
    }

    @Test
    void validateBelongsToNet_nodeHasDifferentParentNet_error() {
        Net net = new Net();
        Node node = net.newCNode(Commands.NOP);
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
        net.setRoot(net.newNode());

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
        private NetEventListener eventListener;

        private Node externalNode;

        @BeforeEach
        void setUp() {
            builder().net(externalNode = value(0));
        }

        @Test
        void getTarget() {
            Node u, nop;
            builder().net(u = unary(nop = value(Commands.NOP)));

            Node uTarget = u.getLeftChild();

            assertThat(uTarget).isSameAs(nop);
        }

        @Test
        void setTarget() {
            Node u, ix;
            Net net = builder().net(u = unary(value(Commands.NOP)), ix = value(Commands.ANCESTOR_EXCHANGE));
            net.addEventListener(eventListener);

            u.setLeftChild(ix, net.getPermissions());

            verify(eventListener).calledSetChild(u, ArrowDirection.LEFT, ix);
            verifyNoMoreInteractions(eventListener);
            assertThat(u.getLeftChild()).isSameAs(ix);
        }

        @Test
        void setTarget_otherNet() throws Exception {
            Node a;
            Net net = builder().net(a = value(0));
            net.addEventListener(eventListener);

            a.setLeftChild(externalNode, net.getPermissions());

            verify(eventListener).calledSetChild(a, ArrowDirection.LEFT, externalNode);
            verifyNoMoreInteractions(eventListener);
            assertThat(a.getLeftChild()).isSameAs(externalNode);
        }

        @Test
        void newUNode_childArg() throws Exception {
            Net net = parser.parse("_a:<o>");
            Node nop = net.getByName("_a");
            net.addEventListener(eventListener);

            Node uNode = net.newUNode(nop, net.getPermissions());

            net.validateBelongsToNet(uNode);
            verify(eventListener).calledNewNode(uNode);
            verifyNoMoreInteractions(eventListener);
            assertThat(uNode.getLeftChild()).isEqualTo(nop);
        }

        @Test
        void newNode() {
            Net net = new Net();
            net.addEventListener(eventListener);

            Node node = net.newNode(net.getPermissions());

            net.validateBelongsToNet(node);
            verify(eventListener).calledNewNode(node);
            verifyNoMoreInteractions(eventListener);
            assertThat(node.getLeftChild()).isEqualTo(node);
            assertThat(node.getRightChild()).isEqualTo(node);
        }

        @Test
        void newBNode_childArgs() throws Exception {
            Net net = parser.parse("_a:<o>; _b:<//x/\\>");
            Node nop = net.getByName("_a");
            Node ix = net.getByName("_b");
            net.addEventListener(eventListener);

            Node bNode = net.newNode(nop, ix, net.getPermissions());

            net.validateBelongsToNet(bNode);
            verify(eventListener).calledNewNode(bNode);
            verifyNoMoreInteractions(eventListener);
            assertThat(bNode.getLeftChild()).isEqualTo(nop);
            assertThat(bNode.getRightChild()).isEqualTo(ix);
        }

        @Test
        void newBNode_childArgs_error_left() throws Exception {
            Net net = parser.parse("_a:<o>");
            Node nop = net.getByName("_a");

            assertThatThrownBy(() -> net.newNode(externalNode, nop))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("node belongs to another net");
        }

        @Test
        void newBNode_childArgs_error_right() throws Exception {
            Net net = parser.parse("_a:<o>");
            Node nop = net.getByName("_a");

            assertThatThrownBy(() -> net.newNode(nop, externalNode))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("node belongs to another net");
        }

        @Test
        void newCNode() {
            Net net = new Net();
            net.addEventListener(eventListener);

            Node cNode = net.newCNode(new NopCommand(), net.getPermissions());

            net.validateBelongsToNet(cNode);
            verify(eventListener).calledNewNode(cNode);
            verifyNoMoreInteractions(eventListener);
        }

        @Nested
        class AncestorExchange {

            @ParameterizedTest
            @MethodSource("ancestorSwapData")
            void ancestorSwap(String input, String expectedOutput) throws Exception {
                Net net = parser.parse(input);
                Node a = net.getByName("a");
                Node b = net.getByName("b");
                net.addEventListener(eventListener);

                Net.ancestorExchange(a, b);

                verifyNoMoreInteractions(eventListener);
                NetFormatter formatter = new NetFormatter()
                        .withIncludeOrphans(true)
                        .withForceGivenNodeNames(true);
                assertThat(formatter.format(net)).isEqualTo(expectedOutput);
            }

            private static Stream<Arguments> ancestorSwapData() {
                return Stream.of(
                        arguments("(a:(#1, #2), b:(#3, #4))", "(b:(#3, #4), a:(#1, #2))"),
                        arguments("[a:#1(<o>, <o>)]; b:#2(<//x/\\>, <//x/\\>)", "[b:#2(<//x/\\>, <//x/\\>)]; a:#1(<o>, <o>)"),
                        arguments("[a:#1(b, b)]; b:#2(<//x/\\>, <//x/\\>)", "[b:#2(<//x/\\>, <//x/\\>)]; a:#1"),
                        arguments("(a:[<//x/\\>], b:[<o>])", "(b:(<o>, a:(<//x/\\>, b)), a)"),
                        arguments("(([a:#0], b:<//x/\\>), ((a, a), [b]))", "(([b:<//x/\\>(a:(b, b), a)], a), ((b, b), [a]))")
                );
            }

            @Test
            void ancestorSwap_error_first() throws Exception {
                Net net = parser.parse("_a:<o>");
                Node nop = net.getByName("_a");

                assertThatThrownBy(() -> Net.ancestorExchange(externalNode, nop))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessage("node belongs to another net");
            }
        }

        @Test
        void ancestorSwap_error_second() throws Exception {
            Net net = parser.parse("_a:<o>");
            Node nop = net.getByName("_a");

            assertThatThrownBy(() -> Net.ancestorExchange(nop, externalNode))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("node belongs to another net");
        }
    }

    @Test
    void referentialGarbageCollection() throws Exception {
        Net net = parser.parse("<o>[#1]; <//x/\\>");

        net.referentialGarbageCollection();

        assertThat(net.getNodesAsList().stream().filter(Objects::nonNull).count()).isEqualTo(2);
    }
}
