package org.ent.net.io.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.assertj.core.api.Assertions;
import org.ent.net.Net;
import org.ent.net.NetController;
import org.ent.net.ReadOnlyNetController;
import org.ent.net.node.BNode;
import org.ent.net.node.CNode;
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
	public void parse_error_unknownIdentifier() throws Exception {
		Assertions.assertThatThrownBy(() -> parser.parse("(A,B); B=A")).isInstanceOf(ParserException.class)
				.hasMessage("Unkown identifier: 'A'");
	}
}
