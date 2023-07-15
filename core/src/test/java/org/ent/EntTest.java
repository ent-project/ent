package org.ent;

import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.accessor.Accessors;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.run.EntRunner;
import org.ent.run.StepResult;
import org.ent.webui.WebUI;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ent.util.NetBuilder.builder;
import static org.ent.util.NetBuilder.ignored;
import static org.ent.util.NetBuilder.node;
import static org.ent.util.NetBuilder.unary;
import static org.ent.util.NetBuilder.value;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class EntTest {

    private static final boolean WEB_UI = false;

    @BeforeAll
    static void enableWebUI() {
        if (WEB_UI) {
            WebUI.setUpJavalin();
        }
    }

    @BeforeAll
    static void setTestEnvironment() {
        Profile.setTest(true);
    }

    @AfterAll
    static void idleForWebUI() {
        if (WEB_UI) {
            WebUI.loopForever();
        }
    }

    public static Stream<Arguments> getPortalIndexData() {
        return Stream.of(
                arguments(0xFFFF, ArrowDirection.LEFT, 0),
                arguments(0xFFFE, ArrowDirection.LEFT, 1),
                arguments(0xFF00, ArrowDirection.LEFT, 255),
                arguments(0xFAAAFF00, ArrowDirection.LEFT, 255),
                arguments(0x0000, ArrowDirection.LEFT, 0xFFFF),
                arguments(0xFFFF0000, ArrowDirection.RIGHT, 0),
                arguments(0xFFFE0000, ArrowDirection.RIGHT, 1),
                arguments(0xFF000000, ArrowDirection.RIGHT, 255),
                arguments(0xFF00FAAA, ArrowDirection.RIGHT, 255),
                arguments(0x0000000, ArrowDirection.LEFT, 0xFFFF)
        );
    }

    @ParameterizedTest
    @MethodSource("getPortalIndexData")
    void getPortalIndex(int value, ArrowDirection direction, int expectedIndex) {
        int actualIndex = Ent.getPortalIndex(value, direction);

        assertThat(actualIndex).isEqualTo(expectedIndex);
    }

    @Nested
    class Domains {

        private Node portalNode;
        private Ent ent;
        private Net domain;
        private EntRunner runner;
        private PortalArrow portalArrow;

        @Test
        void standard_left() {
            ent = builder().ent(unary(Operations.SET_VALUE_OPERATION, portalNode = node(ignored(), value(5))));
            Node x;
            domain = builder().net(x = value(7));
            setUpLeft();

            StepResult result = runner.step();

            assertThat(result).isEqualTo(StepResult.SUCCESS);
            assertThat(x.getValue()).isEqualTo(5);
        }

        @Test
        void standard_right() {
            ent = builder().ent(unary(Commands.get(Operations.SET_VALUE_OPERATION, Accessors.RIGHT, Accessors.LEFT),
                    portalNode = node(value(5), ignored())));
            Node x;
            domain = builder().net(x = value(7));
            setUpRight();

            StepResult result = runner.step();

            assertThat(result).isEqualTo(StepResult.SUCCESS);
            assertThat(x.getValue()).isEqualTo(5);
        }

        @Test
        void goTroughMultiplePortals() {
            ent = builder().ent(
                    unary(Commands.get(Operations.SET_VALUE_OPERATION, Accessors.LEFT_LEFT, Accessors.RIGHT),
                            portalNode = node(
                                    ignored(),
                                    value(5))));
            Node portal2;
            domain = builder().net(portal2 = ignored());
            domain.setPermittedToWrite(false);
            setUpLeft();
            Node x;
            Net domain2 = builder().net(x = value(7));
            ent.addDomain(domain2);
            int portal2Index = ent.addPortal(new PortalArrow(domain2));
            portal2.setValue(portal2Index);

            StepResult result = runner.step();

            assertThat(result).isEqualTo(StepResult.SUCCESS);
            assertThat(x.getValue()).isEqualTo(5);
        }

        @Test
        void setPortalArrow() {
            ent = builder().ent(unary(Commands.get(Operations.SET_OPERATION, Accessors.LEFT, Accessors.LEFT_LEFT), portalNode = node(ignored(), value(5))));
            Node y;
            domain = builder().net(unary(7, y = value(9)));
            setUpLeft();

            StepResult result = runner.step();

            assertThat(result).isEqualTo(StepResult.SUCCESS);
            assertThat(portalArrow.getTarget(Purview.DIRECT)).isSameAs(y);
        }

        @Test
        void setDomainArrowToDomainNode() {
            ent = builder().ent(unary(Commands.get(Operations.SET_OPERATION, Accessors.LEFT_LEFT, Accessors.LEFT_RIGHT),
                    portalNode = ignored()));
            Node y, b;
            domain = builder().net(y = node(value(5), b = value(9)));
            setUpLeft();

            StepResult result = runner.step();

            assertThat(result).isEqualTo(StepResult.SUCCESS);
            assertThat(y.getLeftChild()).isSameAs(b);
        }

        @Nested
        class Error {
            @Test
            void setOwnArrowToDomainNode() {
                Node root;
                ent = builder().ent(root = unary(Commands.get(Operations.SET_OPERATION, Accessors.FLOW, Accessors.LEFT),
                        portalNode = ignored()));
                domain = builder().net(ignored());
                setUpLeft();

                StepResult result = runner.step();

                assertThat(result).isEqualTo(StepResult.COMMAND_EXECUTION_FAILED);
                assertThat(root.getRightChild()).isSameAs(root);
            }

            @Test
            void setPortalArrowToOwnNode() {
                ent = builder().ent(unary(Commands.get(Operations.SET_OPERATION, Accessors.LEFT, Accessors.FLOW),
                        portalNode = ignored()));
                Node y;
                domain = builder().net(y = ignored());
                setUpLeft();

                StepResult result = runner.step();

                assertThat(result).isEqualTo(StepResult.COMMAND_EXECUTION_FAILED);
                assertThat(portalArrow.getTarget(Purview.DIRECT)).isSameAs(y);
            }

            @Test
            void setDomainArrowToOwnNode() {
                ent = builder().ent(unary(Commands.get(Operations.SET_OPERATION, Accessors.LEFT_LEFT, Accessors.FLOW),
                        portalNode = ignored()));
                Node a, b;
                domain = builder().net(a = unary(b = ignored()));
                setUpLeft();

                StepResult result = runner.step();

                assertThat(result).isEqualTo(StepResult.COMMAND_EXECUTION_FAILED);
                assertThat(a.getLeftChild()).isSameAs(b);
            }
        }

        @Nested
        class ReadOnly {

            @Test
            void setValue() {
                ent = builder().ent(unary(Operations.SET_VALUE_OPERATION, portalNode = node(ignored(), value(5))));
                Node x;
                domain = builder().net(x = value(7));
                domain.setPermittedToWrite(false);
                setUpLeft();

                StepResult result = runner.step();

                assertThat(result).isEqualTo(StepResult.COMMAND_EXECUTION_FAILED);
                assertThat(x.getValue()).isEqualTo(7);
            }

            @Test
            void set() {
                ent = builder().ent(unary(Commands.get(Operations.SET_OPERATION, Accessors.LEFT_LEFT, Accessors.LEFT_RIGHT),
                        portalNode = ignored()));
                Node y, a;
                domain = builder().net(y = node(a = value(5), value(9)));
                domain.setPermittedToWrite(false);
                setUpLeft();

                StepResult result = runner.step();

                assertThat(result).isEqualTo(StepResult.COMMAND_EXECUTION_FAILED);
                assertThat(y.getLeftChild()).isSameAs(a);
            }

            @ParameterizedTest
            @ValueSource(booleans = {true, false})
            void ancestorExchange(boolean permittedToWrite) {
                ent = builder().ent(unary(Commands.get(Operations.ANCESTOR_EXCHANGE_OPERATION, Accessors.LEFT_LEFT, Accessors.LEFT_RIGHT_LEFT),
                        portalNode = ignored()));
                Node y, a, b_parent, b;
                domain = builder().net(y = node(a = value(5), b_parent = unary(b = value(9))));
                domain.setPermittedToWrite(permittedToWrite);
                setUpLeft();

                StepResult result = runner.step();

                if (permittedToWrite) {
                    assertThat(result).isEqualTo(StepResult.SUCCESS);
                    assertThat(y.getLeftChild()).isSameAs(b);
                    assertThat(b_parent.getLeftChild()).isSameAs(a);
                } else {
                    assertThat(result).isEqualTo(StepResult.COMMAND_EXECUTION_FAILED);
                    assertThat(y.getLeftChild()).isSameAs(a);
                    assertThat(b_parent.getLeftChild()).isSameAs(b);
                }
            }
        }

        @Test
        void eval_toplevel() {
            ent = builder().ent(unary(Commands.get(Operations.EVAL_OPERATION, Accessors.LEFT), portalNode = ignored()));
            Node y;
            domain = builder().net(unary(Operations.INC_OPERATION, y = value(14)));
            setUpLeft();

            StepResult result = runner.step();

            assertThat(result).isEqualTo(StepResult.SUCCESS);
            assertThat(y.getValue()).isEqualTo(15);
        }

        @Test
        void eval_inner() {
            ent = builder().ent(unary(Commands.get(Operations.EVAL_OPERATION, Accessors.LEFT_LEFT), portalNode = ignored()));
            Node y;
            domain = builder().net(unary(unary(Operations.INC_OPERATION, y = value(14))));
            setUpLeft();

            StepResult result = runner.step();

            assertThat(result).isEqualTo(StepResult.SUCCESS);
            assertThat(y.getValue()).isEqualTo(15);
        }

        @Nested
        class EvalOnly {
            @ParameterizedTest
            @ValueSource(booleans = {true, false})
            void evalFlow_goodCase(boolean permittedToEvalRoot) {
                ent = builder().ent(unary(Commands.get(Operations.EVAL_FLOW_OPERATION, Accessors.LEFT), portalNode = ignored()));
                Node y, domainRoot1, domainRoot2;
                domain = builder().net(domainRoot1 = node(Operations.INC_OPERATION,
                        y = value(14),
                        domainRoot2 = ignored()));
                domain.setPermittedToWrite(false);
                domain.setPermittedToEvalRoot(permittedToEvalRoot);
                setUpLeft();

                StepResult result = runner.step();

                if (permittedToEvalRoot) {
                    assertThat(result).isEqualTo(StepResult.SUCCESS);
                    assertThat(y.getValue()).isEqualTo(15);
                    assertThat(domain.getRoot()).isSameAs(domainRoot2);
                } else {
                    assertThat(result).isEqualTo(StepResult.COMMAND_EXECUTION_FAILED);
                    assertThat(y.getValue()).isEqualTo(14);
                    assertThat(domain.getRoot()).isSameAs(domainRoot1);
                }
            }

            @Nested
            class Forbidden {
                @Test
                void withoutAdvancing() {
                    // It is important that the domain root is advanced after remote execution.
                    // I.e. EVAL_FLOW should work in verifier mode, but EVAL not.
                    // (otherwise you would break with the intended execution flow)
                    ent = builder().ent(unary(Commands.get(Operations.EVAL_OPERATION, Accessors.LEFT), portalNode = ignored()));
                    Node y, domainRoot1;
                    domain = builder().net(domainRoot1 = unary(Operations.INC_OPERATION, y = value(14)));
                    domain.setPermittedToWrite(false);
                    domain.setPermittedToEvalRoot(true);
                    setUpLeft();

                    StepResult result = runner.step();

                    assertThat(result).isEqualTo(StepResult.COMMAND_EXECUTION_FAILED);
                    assertThat(y.getValue()).isEqualTo(14);
                    assertThat(domain.getRoot()).isSameAs(domainRoot1);
                }

                @Test
                void eval_inner() {
                    // can eval the toplevel node, but not an inner node of the domain
                    ent = builder().ent(unary(Commands.get(Operations.EVAL_FLOW_OPERATION, Accessors.LEFT_LEFT), portalNode = ignored()));
                    Node y;
                    domain = builder().net(unary(unary(Operations.INC_OPERATION, y = value(14))));
                    domain.setPermittedToWrite(false);
                    domain.setPermittedToEvalRoot(true);
                    setUpLeft();

                    StepResult result = runner.step();

                    assertThat(result).isEqualTo(StepResult.COMMAND_EXECUTION_FAILED);
                    assertThat(y.getValue()).isEqualTo(14);
                }

                @ParameterizedTest
                @ValueSource(booleans = {true, false})
                void eval_advanceArrow_thenEvalInner(boolean permittedToWrite) {
                    // Advance the portal arrow and then try to eval.
                    // The idea is that the rooted portal arrow can change the domain root.
                    // This should not work in any case.
                    ent = builder().ent(
                            node(Commands.get(Operations.SET_OPERATION, Accessors.LEFT, Accessors.LEFT_RIGHT),
                                    portalNode = ignored(),
                                    unary(Commands.get(Operations.EVAL_FLOW_OPERATION, Accessors.LEFT),
                                            portalNode)));
                    Node y1, y2, domainRoot0, domainRoot1;
                    domain = builder().net(
                            domainRoot0 = node(Operations.INC_OPERATION,
                                    y1 = value(5),
                                    domainRoot1 = unary(Operations.INC_OPERATION,
                                            y2 = value(10))));
                    domain.setPermittedToWrite(permittedToWrite);
                    domain.setPermittedToEvalRoot(true);
                    setUpLeft();

                    StepResult result1 = runner.step();
                    assertThat(result1).isEqualTo(StepResult.SUCCESS);
                    assertThat(portalArrow.getTarget(Purview.DIRECT)).isEqualTo(domainRoot1);
                    assertThat(domain.getRoot()).isEqualTo(domainRoot0);

                    StepResult result2 = runner.step();

                    if (permittedToWrite) {
                        assertThat(result2).isEqualTo(StepResult.SUCCESS);
                        assertThat(y1.getValue()).isEqualTo(5);
                        assertThat(y2.getValue()).isEqualTo(11);
                    } else {
                        assertThat(result2).isEqualTo(StepResult.COMMAND_EXECUTION_FAILED);
                        assertThat(y1.getValue()).isEqualTo(5);
                        assertThat(y2.getValue()).isEqualTo(10);
                    }
                }

                @ParameterizedTest
                @ValueSource(booleans = {true, false})
                void eval_runsOwnCommand(boolean permittedToWrite) {
                    ent = builder().ent(unary(Operations.EVAL_OPERATION,
                            unary(Commands.get(Operations.INC_OPERATION, Accessors.LEFT), portalNode = ignored())));
                    Node y;
                    domain = builder().net(y = value(14));
                    domain.setPermittedToWrite(permittedToWrite);
                    domain.setPermittedToEvalRoot(true);
                    setUpLeft();

                    StepResult result = runner.step();

                    if (permittedToWrite) {
                        assertThat(result).isEqualTo(StepResult.SUCCESS);
                        assertThat(y.getValue()).isEqualTo(15);
                    } else {
                        // eval of <set> in own net must not modify domain nodes
                        assertThat(result).isEqualTo(StepResult.COMMAND_EXECUTION_FAILED);
                        assertThat(y.getValue()).isEqualTo(14);
                    }
                }

                @ParameterizedTest
                @ValueSource(booleans = {true, false})
                void setPortalToInnerNode(boolean permittedToWrite) {
                    // This is a strategy to move the rooted portal arrow (and thereby the domain root) to an arbitrary
                    // inner domain node.
                    // This attempted by eval-flow-ing a SET command with according parameters (left argument refers
                    // to the portal arrow and right argument to the inner target node).
                    // (Currently it may not be possible to make use of the exploit, should it be possible. (The portal
                    // arrow gets overridden at the end of EVAL_FLOW.) It shouldn't happen nonetheless.)
                    ent = builder().ent(unary(Commands.get(Operations.EVAL_FLOW_OPERATION, Accessors.LEFT),
                            portalNode = ignored()));
                    Node portalInDomain, legalDomainRoot2;
                    domain = builder().net(node(Commands.SET,
                            portalInDomain = node(
                                    ignored(),
                                    unary(Commands.get(Operations.INC_OPERATION),
                                            value(12))),
                            legalDomainRoot2 = value(Commands.FINAL_SUCCESS)));
                    domain.setPermittedToWrite(permittedToWrite);
                    domain.setPermittedToEvalRoot(true);
                    setUpLeft();
                    portalInDomain.setValue(portalNode.getValue());

                    List<Node> roots = new ArrayList<>();
                    class SetRootListener extends NopNetEventListener {
                        @Override
                        public void setRoot(Node previousRoot, Node newRoot) {
                            roots.add(newRoot);
                        }
                    }
                    domain.addEventListener(new SetRootListener());

                    runner.step();

                    assertThat(roots).containsExactly(legalDomainRoot2);
                    assertThat(portalArrow.getTarget(Purview.DIRECT)).isSameAs(legalDomainRoot2);
                }
            }
        }

        private void setUp(ArrowDirection direction) {
            this.ent.addDomain(this.domain);
            this.portalArrow = new RootPortalArrow(this.domain);
            int portalIndex = this.ent.addPortal(this.portalArrow);
            int value = switch (direction) {
                case LEFT -> portalIndex;
                case RIGHT -> portalIndex << 16;
            };
            this.portalNode.setValue(value);
            this.runner = new EntRunner(this.ent);
        }

        private void setUpLeft() {
            setUp(ArrowDirection.LEFT);
        }

        private void setUpRight() {
            setUp(ArrowDirection.RIGHT);
        }
    }
}