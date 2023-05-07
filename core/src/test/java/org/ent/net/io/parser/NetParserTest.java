package org.ent.net.io.parser;

import org.ent.Environment;
import org.ent.net.Net;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.node.Node;
import org.ent.net.node.NodeType;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.operation.Operations;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class NetParserTest {

	NetParser parser;

	@BeforeEach
	void setup() {
		parser = new NetParser();
	}

	@BeforeAll
	static void setTestEnvironment() {
		Environment.setTest(true);
	}

	@Nested
	class GoodCase {

		@Test
		void value() throws Exception {
			Net net = parser.parse("#123abc");

			Set<Node> nodes = net.getNodes();
			assertThat(nodes).hasSize(1);
			Node root = net.getRoot();

			assertThat(root.getNodeType()).isEqualTo(NodeType.COMMAND_NODE);
			assertThat(root.getValue()).isEqualTo(0x123abc);
		}

		@Test
		void command() throws Exception {
			Net net = parser.parse("<x>");

			Set<Node> nodes = net.getNodes();
			assertThat(nodes).hasSize(1);
			Node root = net.getRoot();

			assertThat(root.getNodeType()).isEqualTo(NodeType.COMMAND_NODE);
			assertThat(root.getValue()).isEqualTo(Commands.get(Operations.ANCESTOR_EXCHANGE).getValue());
		}

		@Test
		void unary() throws Exception {
			Net net = parser.parse("[<o>]");

			Set<Node> nodes = net.getNodes();
			assertThat(nodes).hasSize(2);
			Node root = net.getRoot();

			assertThat(root.getNodeType()).isEqualTo(NodeType.UNARY_NODE);
			Node nop = root.getLeftChild();
			assertThat(nop.getValue()).isEqualTo(Commands.NOP.getValue());
		}

		@Test
		void valued_unary() throws Exception {
			Net net = parser.parse("#1a[<o>]");

			Set<Node> nodes = net.getNodes();
			assertThat(nodes).hasSize(2);
			Node root = net.getRoot();

			assertThat(root.getNodeType()).isEqualTo(NodeType.UNARY_NODE);
			assertThat(root.getValue()).isEqualTo(0x1a);
		}

		@Test
		void binary() throws Exception {
			Net net = parser.parse("(<o>, <x>)");

			Set<Node> nodes = net.getNodes();
			assertThat(nodes).hasSize(3);
			Node root = net.getRoot();

			assertThat(root.getNodeType()).isEqualTo(NodeType.BINARY_NODE);
			assertThat(root.getLeftChild().getNodeType()).isEqualTo(NodeType.COMMAND_NODE);
			assertThat(root.getLeftChild().getValue()).isEqualTo(Commands.NOP.getValue());
			assertThat(root.getRightChild().getNodeType()).isEqualTo(NodeType.COMMAND_NODE);
			assertThat(root.getRightChild().getValue()).isEqualTo(Commands.get(Operations.ANCESTOR_EXCHANGE).getValue());
		}

		@Test
		void someExample() throws Exception {
			Net net = parser.parse("(<o>, [<o>])");

			Set<Node> nodes = net.getNodes();
			assertThat(nodes).hasSize(4);
			Node root = net.getRoot();
			Node c = root.getLeftChild();
			assertThat(c.getNodeType()).isEqualTo(NodeType.COMMAND_NODE);
			assertThat(c.getCommand()).isEqualTo(Commands.NOP);
			Node rNode = root.getRightChild();
			assertThat(rNode.getNodeType()).isEqualTo(NodeType.UNARY_NODE);
			Node c2 = rNode.getLeftChild();
			assertThat(c2.getNodeType()).isEqualTo(NodeType.COMMAND_NODE);
			assertThat(c2.getCommand()).isEqualTo(Commands.NOP);
		}

		@Test
		void chain() throws Exception {
			Net net = parser.parse("A:B; B:C; C:<o>");

			Set<Node> nodes = net.getNodes();
			assertThat(nodes).hasSize(1);
		}

		@Test
		void selfReference() throws Exception {
			Net net = parser.parse("A:[[A]]");

			Set<Node> nodes = net.getNodes();
			assertThat(nodes).hasSize(2);

			Node u = nodes.iterator().next();
			assertThat(u.getNodeType()).isEqualTo(NodeType.UNARY_NODE);
			assertThat(u.getLeftChild().getLeftChild()).isSameAs(u);
		}

		@Test
		void comment() throws Exception {
			Net net = parser.parse("A:[B]; ~ definition of B follows\nB:A");

			Set<Node> nodes = net.getNodes();
			assertThat(nodes).hasSize(1);
		}

		@Test
		void marker() throws Exception {
			parser.permitMarkerNodes();
			Net net = parser.parse("A:[@]");

			Set<Node> nodes = net.getNodes();
			assertThat(nodes).hasSize(1);
		}

		@ParameterizedTest
		@MethodSource("otherGoodCases")
		void other(String input) throws Exception {
			Net net = parser.parse(input);

			NetFormatter formatter = new NetFormatter().withAscii(true);
			String output = formatter.format(net);
			assertThat(output).isEqualTo(input);
		}

		private static Stream<Arguments> otherGoodCases() {
			return Stream.of(
				arguments("<x>[<o>]"),  	// unary with command value
				arguments("<=>(<o>, <o>)"), // binary with command value
				arguments("#1a(<o>, <o>)"), // binary with value
				arguments("[#abc]"), 		// value inside unary
				arguments("(#1, #2)"), 		// values inside binary
				arguments("A:#1(<o>, [A])"), // label and value on binary
				arguments("a:#1[[a]]"), 	// label and value on unary
				arguments("_a:<x>; [_a]"), 	// name command and use it later
				arguments("#1; #2; <o>"), 	// multiple values
				arguments("A:((<x>, A), a:[[[[a]]]])")
			);
		}
	}

	@Nested
	class Error {
		@Test
		void markerNotPermitted() throws Exception {
			assertThatThrownBy(() -> parser.parse("A:[@]")).isInstanceOf(ParserException.class)
					.hasMessage("Found marker node in line 1, column 6, but is not permitted");
		}

		@Test
		void markerTopLevel() throws Exception {
			parser.permitMarkerNodes();
			assertThatThrownBy(() -> parser.parse("<o>; @")).isInstanceOf(ParserException.class)
					.hasMessage("Top level node must not be a marker node");
		}

		@Test
		void tokenizer() throws Exception {
			assertThatThrownBy(() -> parser.parse("A:[B];\nC:]")).isInstanceOf(ParserException.class)
					.hasMessageContaining("Unexpected token ']' in line 2, column 5");
		}

		@Test
		void unknownIdentifier() throws Exception {
			assertThatThrownBy(() -> parser.parse("(A,B); B:A")).isInstanceOf(ParserException.class)
					.hasMessage("Unknown identifier: 'A'");
		}

		@Test
		void duplicateIdentifier() throws Exception {
			assertThatThrownBy(() -> parser.parse("(a:#1, a:#2)")).isInstanceOf(ParserException.class)
					.hasMessage("Identifier 'a' bound more than once.");
		}
	}

	@Test
	void getNodeNames() throws Exception {
		Net net = parser.parse("(nop:<o>, [[C]]); C:[nop];");

		Map<String, Node> nodeNames = parser.getNodeNames();
		assertThat(nodeNames).containsOnlyKeys("nop", "C");
		assertThat(net.getNodes()).contains(nodeNames.get("nop"), nodeNames.get("C"));
	}

	@Test
	void getMainNodes() throws Exception {
		parser.parse("T:(x:[(x,x)],K);K:((<o>,F),F);F:x");

		List<Node> mainNodes = parser.getMainNodes();
		assertThat(mainNodes).hasSize(3);

		Map<String, Node> nodeNames = parser.getNodeNames();
		assertThat(mainNodes).asList().containsExactly(nodeNames.get("T"), nodeNames.get("K"),
				nodeNames.get("F"));
	}

}
