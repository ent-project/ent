package org.ent.net;

import org.ent.net.node.Node;
import org.ent.net.node.cmd.Commands;

import java.util.Arrays;
import java.util.List;

public class NetTestData {

	public final NetWithStringRepresentation net0, net1, net2;

	public final NetWithStringRepresentation netDeep;

	public final List<NetWithStringRepresentation> all;

    public NetTestData() {
    	net0 = buildNet0();
    	net1 = buildNet1();
    	net2 = buildNet2();
    	netDeep = buildNetDeep();
    	all = Arrays.asList(net0, net1, net2, netDeep);
	}

	public static NetWithStringRepresentation buildNet0() {
    	Net net = new Net();
		Node u = net.newNode();
		u.setLeftChild(net.newUNode(u));
		Node nop = net.newCNode(Commands.NOP);

		Node root = net.newRoot();
		root.setLeftChild(u);
		root.setRightChild(nop);

        return new NetWithStringRepresentation(net, "(a:[[a]], <o>)");
    }

	public static NetWithStringRepresentation buildNet1() {
    	Net net = new Net();

        Node a = net.newUNode(net.newCNode(Commands.NOP));
		Node b1 = net.newNode(a, net.newCNode(Commands.ANCESTOR_EXCHANGE));
		b1.setValue(0x1f);
		Node root = net.newNode(a, b1);

        net.setRoot(root);

        return new NetWithStringRepresentation(net, "(a:[<o>], #1f(a, <x>))");
    }

	public static NetWithStringRepresentation buildNet2() {
    	Net net = new Net();

		Node b3 = net.newNode();
		Node b4 = net.newNode();
		Node b2 = net.newNode(b4, b3);				// A
		Node b1 = net.newNode(b4, b2);
		Node unary_b = net.newUNode(b1);			// b
		Node unary_root_a = net.newUNode(unary_b);	// a
		Node nop = net.newCNode(Commands.NOP);

		b3.setLeftChild(b4);
		b3.setRightChild(unary_b);
		b4.setLeftChild(nop);
		b4.setRightChild(unary_root_a);

		net.setRoot(unary_root_a);
        return new NetWithStringRepresentation(net, "a:[b:[(A:(<o>, a), (A, (A, b)))]]");
    }

	public NetWithStringRepresentation buildNetDeep() {
    	Net net = new Net();

		Node n = net.newCNode(Commands.NOP);
        for (int i = 0; i < 20; i++) {
        	n = net.newUNode(n);
        }

        net.setRoot(n);

        return new NetWithStringRepresentation(net, "[[[[[[[[[[[[[[[[[[[[<o>]]]]]]]]]]]]]]]]]]]]");
    }
}
