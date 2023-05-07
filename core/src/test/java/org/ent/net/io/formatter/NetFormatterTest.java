package org.ent.net.io.formatter;

import org.ent.Environment;
import org.ent.net.Net;
import org.ent.net.NetTestData;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Commands;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ent.net.node.cmd.operation.Operations.ANCESTOR_EXCHANGE;
import static org.ent.net.node.cmd.operation.Operations.SET;

class NetFormatterTest {

	private static NetTestData testData;

	private NetFormatter formatter;

	@BeforeAll
	public static void setUpTestData() {
		testData = new NetTestData();
	}

	@BeforeAll
	public static void enableTestEnvironment() {
		Environment.setTest(true);
	}

	@BeforeEach
	void setUp() {
		formatter = new NetFormatter().withAscii(true);
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
				root.setCommand(Commands.get(SET));

				String str = formatter.format(net);

				assertThat(str).isEqualTo("<=>");
			}

			@Test
			void unary() {
				root.setLeftChild(net.newCNode(Commands.get(SET)), Purview.DIRECT);

				String str = formatter.format(net);

				assertThat(str).isEqualTo("[<=>]");
			}

			@Test
			void unaryWithValue() {
				root.setValue(0x1a);
				root.setLeftChild(net.newCNode(Commands.get(SET)), Purview.DIRECT);

				String str = formatter.format(net);

				assertThat(str).isEqualTo("#1a[<=>]");
			}

			@Test
			void unaryWithCommand() {
				root.setCommand(Commands.get(ANCESTOR_EXCHANGE));
				root.setLeftChild(net.newCNode(Commands.get(SET)), Purview.DIRECT);

				String str = formatter.format(net);

				assertThat(str).isEqualTo("<x>[<=>]");
			}

			@Test
			void binary() {
				root.setLeftChild(net.newCNode(Commands.get(SET)), Purview.DIRECT);
				root.setRightChild(net.newCNode(Commands.get(ANCESTOR_EXCHANGE)), Purview.DIRECT);

				String str = formatter.format(net);

				assertThat(str).isEqualTo("(<=>, <x>)");
			}

			@Test
			void binaryWithValue() {
				root.setValue(0xabc);
				root.setLeftChild(net.newCNode(Commands.get(SET)), Purview.DIRECT);
				root.setRightChild(net.newCNode(Commands.get(ANCESTOR_EXCHANGE)), Purview.DIRECT);

				String str = formatter.format(net);

				assertThat(str).isEqualTo("#abc(<=>, <x>)");
			}

			@Test
			void rightUnary() {
				root.setRightChild(net.newCNode(Commands.get(ANCESTOR_EXCHANGE)), Purview.DIRECT);

				String str = formatter.format(net);

				assertThat(str).isEqualTo("A:(A, <x>)");
			}
		}

		@Nested
		class Names {
			@Test
			void doubleChildCommand() {
				Node n = net.newNode();
				root.setLeftChild(n, Purview.DIRECT);
				root.setRightChild(n, Purview.DIRECT);

				String str = formatter.format(net);

				assertThat(str).isEqualTo("(_a:<o>, _a)");
			}

			@Test
			void doubleChildUnary() {
				Node u = net.newUNode(net.newNode());
				root.setLeftChild(u, Purview.DIRECT);
				root.setRightChild(u, Purview.DIRECT);

				String str = formatter.format(net);

				assertThat(str).isEqualTo("(a:[<o>], a)");
			}

			@Test
			void doubleChildBinary() {
				Node b = net.newBNode(net.newCNode(Commands.get(SET)), net.newCNode(Commands.get(ANCESTOR_EXCHANGE)));
				root.setLeftChild(b, Purview.DIRECT);
				root.setRightChild(b, Purview.DIRECT);

				String str = formatter.format(net);

				assertThat(str).isEqualTo("(A:(<=>, <x>), A)");
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
			net.getNodes().remove(u1.getLeftChild());
			net.getNodes().remove(u1);

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

			String str = formatter.format(net);

			assertThat(str).isEqualTo("A:(a:[[a]], <o>); [A]");
		}

		@Test
		void setNodeNames() {
			Map<Node, String> nodeNames = new HashMap<>();
			nodeNames.put(root, "x1");
			formatter.setNodeNames(nodeNames);
			Node u = net.newUNode(root);
			root.setLeftChild(u);

			String str = formatter.format(net);

			assertThat(str).isEqualTo("x1:[[x1]]");
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
	void format(Net net, String stringRepresentation) throws Exception {
		String str = formatter.format(net);

		assertThat(str).isEqualTo(stringRepresentation);
	}

	private static Stream<Arguments> format_testData() {
		return testData.all.stream().map(nws -> Arguments.of(nws.getNet(), nws.getStringRepresentation()));
	}


	@Test
	void format_3Calls() {
		String str0 = formatter.format(testData.net0.getNet());
		String str1 = formatter.format(testData.net1.getNet());
		String str2 = formatter.format(testData.net2.getNet());

		assertThat(str0).isEqualTo("(a:[[a]], <o>)");
		assertThat(str1).isEqualTo("(b:[<o>], #1f(b, <x>))");
		assertThat(str2).isEqualTo("c:[d:[(A:(<o>, c), (A, (A, d)))]]");
	}


	@Test
	void format_maxDepth() {
		Net net = testData.buildNetDeep().getNet();
		formatter.setMaxDepth(3);

		assertThat(formatter.format(net)).isEqualTo("[[[...]]]");
	}
}
