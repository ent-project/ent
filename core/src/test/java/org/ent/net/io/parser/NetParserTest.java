package org.ent.net.io.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ent.net.Net;
import org.ent.net.NetController;
import org.ent.net.ReadOnlyNetController;
import org.ent.net.node.BNode;
import org.ent.net.node.CNode;
import org.ent.net.node.MarkerNode;
import org.ent.net.node.Node;
import org.ent.net.node.UNode;
import org.ent.net.node.cmd.CommandFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NetParserTest {

	NetController controller;

	NetParser parser;

	@BeforeEach
	public void setup() {
		controller = ReadOnlyNetController.getInstance();
		parser = new NetParser();
	}

	@Test
	public void parse_okay_example() throws Exception {
		Net net = parser.parse("(<nop>, [<nop>])");

		Set<Node> nodes = net.getNodes();
		assertThat(nodes.size()).isEqualTo(4);
		Node root = net.getRoot();
		assertThat(root).isInstanceOf(BNode.class);
		BNode bRoot = (BNode) root;
		assertThat(bRoot.getLeftChild(controller)).isInstanceOfSatisfying(CNode.class, c -> {
			assertThat(c.getCommand()).isEqualTo(CommandFactory.getByName("nop"));
		});
		Node rNode = bRoot.getRightChild(controller);
		assertThat(rNode).isInstanceOf(UNode.class);
		UNode urNode = (UNode) rNode;
		assertThat(urNode.getChild(controller)).isInstanceOfSatisfying(CNode.class, c -> {
			assertThat(c.getCommand()).isEqualTo(CommandFactory.getByName("nop"));
		});
	}

	@Test
	public void parse_okay_chain() throws Exception {
		Net net = parser.parse("A=B; B=C; C=<nop>");

		Set<Node> nodes = net.getNodes();
		assertThat(nodes.size()).isEqualTo(1);
	}

	@Test
	public void parse_okay_selfReference() throws Exception {
		Net net = parser.parse("A=[A]");

		Set<Node> nodes = net.getNodes();
		assertThat(nodes.size()).isEqualTo(1);

		Node n = nodes.iterator().next();
		assertThat(n).isInstanceOfSatisfying(UNode.class, uNode -> {
			assertThat(uNode.getChild(controller)).isSameAs(uNode);
		});
	}

	@Test
	public void parse_okay_comment() throws Exception {
		Net net = parser.parse("A=[B]; @ definition of B follows\nB=A");

		Set<Node> nodes = net.getNodes();
		assertThat(nodes.size()).isEqualTo(1);
	}

	@Test
	public void parse_okay_marker() throws Exception {
		parser.permitMarkerNodes(new MarkerNode());
		Net net = parser.parse("A=[#]");

		Set<Node> nodes = net.getNodes();
		assertThat(nodes.size()).isEqualTo(1);
	}

	@Test
	public void parse_error_markerNotPermitted() throws Exception {
		assertThatThrownBy(() -> parser.parse("A=[#]")).isInstanceOf(ParserException.class)
				.hasMessage("Found marker node in line 1, column 6, but is not permitted");
	}

	@Test
	public void parse_error_markerTopLevel() throws Exception {
		parser.permitMarkerNodes(new MarkerNode());
		assertThatThrownBy(() -> parser.parse("<nop>; #")).isInstanceOf(ParserException.class)
				.hasMessage("Top level node must not be a marker node");
	}

	@Test
	public void parse_error_tokenizer() throws Exception {
		assertThatThrownBy(() -> parser.parse("A=[B];\nC=]")).isInstanceOf(ParserException.class)
				.hasMessageContaining("Unexpected token ']' in line 2, column 5");
	}

	@Test
	public void parse_error_unknownIdentifier() throws Exception {
		assertThatThrownBy(() -> parser.parse("(A,B); B=A")).isInstanceOf(ParserException.class)
				.hasMessage("Unkown identifier: 'A'");
	}

	@Test
	public void getNodeNames() throws Exception {
		Net net = parser.parse("(nop=<nop>, [[C]]); C=[nop];");

		Map<String, Node> nodeNames = parser.getNodeNames();
		assertThat(nodeNames).containsOnlyKeys("nop", "C");
		assertThat(net.getNodes()).contains(nodeNames.get("nop"), nodeNames.get("C"));
	}

	@Test
	public void getMainNodes() throws Exception {
		parser.parse("T=(x=[(x,x)],K);K=((<nop>,F),F);F=x");

		List<Node> mainNodes = parser.getMainNodes();
		assertThat(mainNodes).hasSize(3);

		Map<String, Node> nodeNames = parser.getNodeNames();
		assertThat(mainNodes).asList().containsExactly(nodeNames.get("T"), nodeNames.get("K"),
				nodeNames.get("F"));
	}

}
