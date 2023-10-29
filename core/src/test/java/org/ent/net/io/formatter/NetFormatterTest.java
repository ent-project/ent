package org.ent.net.io.formatter;

import org.ent.Ent;
import org.ent.Profile;
import org.ent.net.CopyValueGameTestSetup;
import org.ent.net.Net;
import org.ent.net.NetTestData;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.accessor.Accessors;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.net.node.cmd.split.Splits;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ent.net.node.cmd.operation.Operations.ANCESTOR_EXCHANGE_OPERATION;
import static org.ent.net.node.cmd.operation.Operations.SET_OPERATION;
import static org.ent.net.node.cmd.split.Conditions.GREATER_THAN_CONDITION;
import static org.ent.util.NetBuilder.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class NetFormatterTest {

	private static NetTestData testData;

	private NetFormatter formatter;

	@BeforeAll
	public static void setUpTestData() {
		testData = new NetTestData();
	}

	@BeforeAll
	public static void enableTestEnvironment() {
		Profile.setTest(true);
	}

	@BeforeEach
	void setUp() {
		formatter = new NetFormatter();
	}

	@Nested
	class Format {

		private Net net;
		private Node root;

		@BeforeEach
		void setUp() {
			net = new Net();
			root = net.newRoot();
		}

		@Nested
		class Simple {

			@Test
			void value() {
				root.setValue(0x2a00bf01);

				String str = formatter.format(net);

				assertThat(str).isEqualTo("#2a00bf01");
			}

			@Test
			void command() {
				root.setCommand(Commands.get(SET_OPERATION));

				String str = formatter.format(net);

				assertThat(str).isEqualTo("<//::/\\>");
			}

			@Test
			void split() {
				root.setSplit(Splits.get(GREATER_THAN_CONDITION));

				String str = formatter.format(net);

				assertThat(str).isEqualTo("<?/gt\\?>");
			}

			@Test
			void unary() {
				root.setLeftChild(net.newCNode(Commands.get(SET_OPERATION)));

				String str = formatter.format(net);

				assertThat(str).isEqualTo("[<//::/\\>]");
			}

			@Test
			void unaryWithValue() {
				root.setValue(0x1a);
				root.setLeftChild(net.newCNode(Commands.get(SET_OPERATION)));

				String str = formatter.format(net);

				assertThat(str).isEqualTo("#1a[<//::/\\>]");
			}

			@Test
			void unaryWithCommand() {
				root.setCommand(Commands.get(ANCESTOR_EXCHANGE_OPERATION));
				root.setLeftChild(net.newCNode(Commands.get(SET_OPERATION)));

				String str = formatter.format(net);

				assertThat(str).isEqualTo("<//x/\\>[<//::/\\>]");
			}

			@Test
			void binary() {
				root.setLeftChild(net.newCNode(Commands.get(SET_OPERATION)));
				root.setRightChild(net.newCNode(Commands.get(ANCESTOR_EXCHANGE_OPERATION)));

				String str = formatter.format(net);

				assertThat(str).isEqualTo("(<//::/\\>, <//x/\\>)");
			}

			@Test
			void binaryWithValue() {
				root.setValue(0xabc);
				root.setLeftChild(net.newCNode(Commands.get(SET_OPERATION)));
				root.setRightChild(net.newCNode(Commands.get(ANCESTOR_EXCHANGE_OPERATION)));

				String str = formatter.format(net);

				assertThat(str).isEqualTo("#abc(<//::/\\>, <//x/\\>)");
			}

			@Test
			void rightUnary() {
				root.setRightChild(net.newCNode(Commands.get(ANCESTOR_EXCHANGE_OPERATION)));

				String str = formatter.format(net);

				assertThat(str).isEqualTo("A:(A, <//x/\\>)");
			}
		}

		@Nested
		class Names {
			@Test
			void doubleChildCommand() {
				Node n = net.newNode();
				root.setLeftChild(n);
				root.setRightChild(n);

				String str = formatter.format(net);

				assertThat(str).isEqualTo("(_a:#0, _a)");
			}

			@Test
			void doubleChildUnary() {
				Node u = net.newUNode(net.newNode());
				root.setLeftChild(u);
				root.setRightChild(u);

				String str = formatter.format(net);

				assertThat(str).isEqualTo("(a:[#0], a)");
			}

			@Test
			void doubleChildBinary() {
				Node b = net.newNode(net.newCNode(Commands.get(SET_OPERATION)), net.newCNode(Commands.get(ANCESTOR_EXCHANGE_OPERATION)));
				root.setLeftChild(b);
				root.setRightChild(b);

				String str = formatter.format(net);

				assertThat(str).isEqualTo("(A:(<//::/\\>, <//x/\\>), A)");
			}

			@Test
			void selfReferenceRoot() {
				Node u = net.newUNode(root);
				root.setLeftChild(u);

				String str = formatter.format(net);

				assertThat(str).isEqualTo("a:[[a]]");
			}
		}

		@Test
		void twoCalls_keepLabel() {
			Node u1 = net.newNode();
			u1.setLeftChild(net.newUNode(u1));
			Node u2 = net.newNode();
			u2.setLeftChild(net.newUNode(u2));
			root.setLeftChild(u1);
			root.setRightChild(u2);

			String str1 = formatter.format(net);

			assertThat(str1).isEqualTo("(a:[[a]], b:[[b]])");

			root.setLeftChild(net.newCNode(Commands.NOP));
			net.removeNode(u1.getLeftChild());
			net.removeNode(u1);

			String str2 = formatter.format(net);

			assertThat(str2).isEqualTo("(<o>, b:[[b]])");
		}

		@Test
		void multipleRoots() {
			Node u1 = net.newNode();
			u1.setLeftChild(net.newUNode(u1));
			root.setLeftChild(u1);
			root.setRightChild(net.newCNode(Commands.NOP));
			net.newUNode(root);
			net.setRoot(root);

			formatter.setIncludeOrphans(true);
			String str = formatter.format(net);

			assertThat(str).isEqualTo("A:(a:[[a]], <o>); [A]");
		}

		@Test
		void marker() {
			Node marker = net.permitMarkerNode();
			root.setLeftChild(marker);

			String str = formatter.format(net);

			assertThat(str).isEqualTo("[@]");
		}

	}

	@ParameterizedTest(name = "{index} => format(...) should return {1}")
	@MethodSource("format_testData")
	void format(Net net, String stringRepresentation) {
		String str = formatter.format(net);

		assertThat(str).isEqualTo(stringRepresentation);
	}

	private static Stream<Arguments> format_testData() {
		return testData.all.stream().map(nws -> arguments(nws.getNet(), nws.getStringRepresentation()));
	}


	@Test
	void format_3Calls() {
		String str0 = formatter.format(testData.net0.getNet());
		String str1 = formatter.format(testData.net1.getNet());
		String str2 = formatter.format(testData.net2.getNet());

		assertThat(str0).isEqualTo("(a:[[a]], <o>)");
		assertThat(str1).isEqualTo("(a:[<o>], #1f(a, <//x/\\>))");
		assertThat(str2).isEqualTo("a:[b:[(A:(<o>, a), (A, (A, b)))]]");
	}


	@Test
	void format_maxDepth() {
		Net net = testData.buildNetDeep().getNet();
		formatter.setMaxDepth(3);

		assertThat(formatter.format(net)).isEqualTo("[[[...]]]");
	}

	@Nested
	class CopyValueGameExample extends CopyValueGameTestSetup {
		@Test
		void format() {
			build();

			String str = formatter.format(ent);

			assertThat(str).isEqualTo("""
					<////=///\\>(a:[A], <eval_flow//>(a, <eval_flow//>(a, <SUCCESS>)))
					input { x:#0 }
					verifier { A:<o>(data:(x, #7), <\\::/\\>(<?///==//\\?>(data, <SUCCESS>), <FAILURE>)) }"""
			);
		}
	}

    @Test
    void withDomain() {
        Net domain = builder().net(node(value(1), value(2)));
        domain.setName("ant");
        Ent ent = builder().ent(
                node(Commands.get(Operations.SET_OPERATION, Accessors.R, Accessors.LL), domain.getRoot(), ignored()));
        ent.addDomain(domain);

        String str = formatter.format(ent);

        assertThat(str).isEqualTo("""
                <\\:://>(A, #0)
                ant { A:(#1, #2) }""");
    }
}
