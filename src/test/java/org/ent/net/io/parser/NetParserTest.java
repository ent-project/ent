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
import org.junit.jupiter.api.Test;

public class NetParserTest {

	NetController controller = ReadOnlyNetController.getInstance();

	@Test
	public void parse_okay() throws Exception {
		NetParser parser = new NetParser();

		Net net = parser.parse("(<nop>, [<nop>])");

		Set<Node> nodes = net.getNodes();
		Assertions.assertThat(nodes.size()).isEqualTo(4);
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

}
