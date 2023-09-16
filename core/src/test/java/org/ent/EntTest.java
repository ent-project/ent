package org.ent;

import org.ent.net.Net;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.accessor.Accessors;
import org.ent.net.node.cmd.operation.BiOperation;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.permission.WriteFacet;
import org.ent.run.EntRunner;
import org.ent.run.StepResult;
import org.ent.webui.WebUI;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ent.util.NetBuilder.builder;
import static org.ent.util.NetBuilder.ignored;
import static org.ent.util.NetBuilder.node;
import static org.ent.util.NetBuilder.unary;
import static org.ent.util.NetBuilder.value;

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

    @Nested
    class Domains {
        private Ent ent;
        private Net domain;
        private EntRunner runner;

        @Test
        void standard_left() {
            Node x;
            domain = builder().net(x = value(7));
            ent = builder().ent(unary(Operations.SET_VALUE_OPERATION, node(x, value(5))));
            setUp();
            ent.putPermissions(p -> p.net(np -> np.canWrite(domain, WriteFacet.VALUE)));

            StepResult result = runner.step();

            assertThat(result).isEqualTo(StepResult.SUCCESS);
            assertThat(x.getValue()).isEqualTo(5);
        }

        @Test
        void standard_right() {
            Node x;
            domain = builder().net(x = value(7));
            ent = builder().ent(unary(Commands.get(Operations.SET_VALUE_OPERATION, Accessors.LR, Accessors.LL),
                    node(value(5), x)));
            setUp();
            ent.putPermissions(p -> p.net(np -> np.canWrite(domain, WriteFacet.VALUE)));

            StepResult result = runner.step();

            assertThat(result).isEqualTo(StepResult.SUCCESS);
            assertThat(x.getValue()).isEqualTo(5);
        }

        @Test
        void setDomainArrowToDomainNode() {
            Node y, b;
            domain = builder().net(y = node(value(5), b = value(9)));
            ent = builder().ent(unary(Commands.get(Operations.SET_OPERATION, Accessors.LL, Accessors.LR), y));
            setUp();
            ent.putPermissions(p -> p.net(np -> np.canWrite(domain, WriteFacet.ARROW)));

            StepResult result = runner.step();

            assertThat(result).isEqualTo(StepResult.SUCCESS);
            assertThat(y.getLeftChild()).isSameAs(b);
        }

        @Test
        void setNetArrowToDomainNode() {
            Node x;
            domain = builder().net(x = value(5));
            Node a;
            ent = builder().ent(unary(Commands.get(Operations.SET_OPERATION, Accessors.LL, Accessors.LR),
                    a = node(ignored(), x)));
            setUp();
            ent.putPermissions(p -> p.net(np -> np.canPointTo(domain)));

            StepResult result = runner.step();

            assertThat(result).isEqualTo(StepResult.SUCCESS);
            assertThat(a.getLeftChild()).isSameAs(x);
        }

        @Nested
        class ReadOnly {

            @ParameterizedTest
            @ValueSource(booleans = {false, true})
            void setValue(boolean canWriteValues) {
                Node x;
                domain = builder().net(x = value(7));
                ent = builder().ent(unary(Operations.SET_VALUE_OPERATION, node(domain.getRoot(), value(5))));
                setUp();
                ent.putPermissions(p -> p.net(np -> {
                    if (canWriteValues) {
                        np.canWrite(domain, WriteFacet.VALUE);
                    }
                }));

                StepResult result = runner.step();

                if (canWriteValues) {
                    assertThat(result).isEqualTo(StepResult.SUCCESS);
                    assertThat(x.getValue()).isEqualTo(5);
                } else {
                    assertThat(result).isEqualTo(StepResult.COMMAND_EXECUTION_FAILED);
                    assertThat(x.getValue()).isEqualTo(7);
                }
            }

            @ParameterizedTest
            @ValueSource(booleans = {false, true})
            void set(boolean canWriteArrows) {
                Node y, a;
                domain = builder().net(y = node(a = value(5), value(9)));
                ent = builder().ent(unary(Commands.get(Operations.SET_OPERATION, Accessors.LL, Accessors.LR),
                        domain.getRoot()));
                setUp();
                ent.putPermissions(p -> p.net(np -> {
                    if (canWriteArrows) {
                        np.canWrite(domain, WriteFacet.ARROW);
                    }
                }));

                StepResult result = runner.step();

                if (canWriteArrows) {
                    assertThat(result).isEqualTo(StepResult.SUCCESS);
                    assertThat(y.getLeftChild()).isNotSameAs(a);
                } else {
                    assertThat(result).isEqualTo(StepResult.COMMAND_EXECUTION_FAILED);
                    assertThat(y.getLeftChild()).isSameAs(a);
                }
            }

            @ParameterizedTest
            @ValueSource(booleans = {true, false})
            void ancestorExchange(boolean canWriteArrows) {
                Node y, a, b_parent, b;
                domain = builder().net(y = node(a = value(5), b_parent = unary(b = value(9))));
                ent = builder().ent(unary(Commands.get(Operations.ANCESTOR_EXCHANGE_OPERATION, Accessors.LL, Accessors.LRL),
                        domain.getRoot()));
                setUp();
                ent.putPermissions(p -> p.net(np -> {
                    if (canWriteArrows) {
                        np.canWrite(domain, WriteFacet.ARROW);
                    }
                }));

                StepResult result = runner.step();

                if (canWriteArrows) {
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
            Node y;
            domain = builder().net(unary(Operations.INC_OPERATION, y = value(14)));
            ent = builder().ent(unary(Commands.get(Operations.EVAL_OPERATION, Accessors.L), domain.getRoot()));
            setUp();
            ent.putPermissions(p -> p.net(np -> np.canWrite(domain, WriteFacet.VALUE)));

            StepResult result = runner.step();

            assertThat(result).isEqualTo(StepResult.SUCCESS);
            assertThat(y.getValue()).isEqualTo(15);
        }

        @Test
        void eval_inner() {
            Node y;
            domain = builder().net(unary(unary(Operations.INC_OPERATION, y = value(14))));
            ent = builder().ent(unary(Commands.get(Operations.EVAL_OPERATION, Accessors.LL), domain.getRoot()));
            setUp();
            ent.putPermissions(p -> p.net(np -> np.canWrite(domain, WriteFacet.VALUE)));

            StepResult result = runner.step();

            assertThat(result).isEqualTo(StepResult.SUCCESS);
            assertThat(y.getValue()).isEqualTo(15);
        }

        @Nested
        class EvalOnly {
            @ParameterizedTest
            @ValueSource(booleans = {true, false})
            void evalFlow_goodCase(boolean permittedToEvalRoot) {
                Node y, domainRoot1, domainRoot2;
                domain = builder().net(domainRoot1 = node(Operations.INC_OPERATION,
                        y = value(14),
                        domainRoot2 = ignored()));
                ent = builder().ent(unary(Commands.get(Operations.EVAL_FLOW_OPERATION, Accessors.L), domainRoot1));
                setUp();
                ent.putPermissions(p -> p.net(n -> {
                    if (permittedToEvalRoot) {
                        n.canExecute(domain);
                    }
                }));

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
                    Node y, domainRoot1;
                    domain = builder().net(domainRoot1 = unary(Operations.INC_OPERATION, y = value(14)));
                    ent = builder().ent(unary(Commands.get(Operations.EVAL_OPERATION, Accessors.L), domain.getRoot()));
                    setUp();
                    ent.putPermissions(p -> p.net(n -> n.canExecute(domain)));

                    StepResult result = runner.step();

                    assertThat(result).isEqualTo(StepResult.COMMAND_EXECUTION_FAILED);
                    assertThat(y.getValue()).isEqualTo(14);
                    assertThat(domain.getRoot()).isSameAs(domainRoot1);
                }

                @Test
                void eval_inner() {
                    // can eval the toplevel node, but not an inner node of the domain
                    Node y;
                    domain = builder().net(unary(unary(Operations.INC_OPERATION, y = value(14))));
                    ent = builder().ent(unary(Commands.get(Operations.EVAL_FLOW_OPERATION, Accessors.LL), domain.getRoot()));
                    setUp();
                    ent.putPermissions(p -> p.net(n -> n.canExecute(domain)));

                    StepResult result = runner.step();

                    assertThat(result).isEqualTo(StepResult.COMMAND_EXECUTION_FAILED);
                    assertThat(y.getValue()).isEqualTo(14);
                }

                @ParameterizedTest
                @ValueSource(booleans = {true, false})
                void eval_runsOwnCommand(boolean permittedToWrite) {
                    Node y;
                    domain = builder().net(y = value(14));
                    ent = builder().ent(unary(Operations.EVAL_OPERATION,
                            unary(Commands.get(Operations.INC_OPERATION, Accessors.L), domain.getRoot())));
                    setUp();
                    ent.putPermissions(p -> p.net(n -> {
                        n.canExecute(domain);
                        if (permittedToWrite) {
                            n.canWrite(domain, WriteFacet.VALUE);
                        }
                    }));

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
                @MethodSource("duplicate_source")
                void duplicate(BiOperation operation) {
                    // verify, that you cannot duplicate a node inside the read-only domain
                    domain = builder().net(node(Commands.NOP, value(3), value(7)));
                    ent = builder().ent(unary(Commands.get(operation, Accessors.LL, Accessors.LR),
                            domain.getRoot()));
                    setUp();
                    ent.putPermissions(p -> p.net(n -> n.canExecute(domain)));
                    assertThat(domain.getNodes()).hasSize(3);

                    StepResult result = runner.step();

                    assertThat(result).isEqualTo(StepResult.COMMAND_EXECUTION_FAILED);
                    assertThat(domain.getNodes()).hasSize(3);
                }

                private static Stream<BiOperation> duplicate_source() {
                    return Stream.of(Operations.DUP_OPERATION, Operations.DUP_NORMAL_OPERATION);
                }
            }
        }

        private void setUp() {
            this.ent.addDomain(this.domain);
            this.runner = new EntRunner(this.ent);
        }
    }
}