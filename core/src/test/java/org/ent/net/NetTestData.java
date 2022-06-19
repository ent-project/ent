package org.ent.net;

import java.util.Arrays;
import java.util.List;

import org.ent.net.node.BNode;
import org.ent.net.node.CNode;
import org.ent.net.node.Node;
import org.ent.net.node.UNode;
import org.ent.net.node.cmd.CommandFactory;
import org.ent.net.node.cmd.NopCommand;

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
    	net.runWithMarkerNode(dummy -> {
            BNode b1 = net.newBNode(dummy, dummy);
            UNode u1 = net.newUNode(dummy);
            CNode nop = net.newCNode(CommandFactory.NOP_COMMAND);

            b1.setLeftChild(u1, Purview.DIRECT);
            b1.setRightChild(nop, Purview.DIRECT);
            u1.setChild(u1, Purview.DIRECT);

            net.setRoot(b1);
    	});

        return new NetWithStringRepresentation(net, "(a=[a], <nop>)");
    }

	public static NetWithStringRepresentation buildNet1() {
    	Net net = new Net();

        UNode a = net.newUNode(net.newCNode(new NopCommand()));
        BNode b1 = net.newBNode(a, net.newCNode(new NopCommand()));
        BNode b2 = net.newBNode(a, b1);

        net.setRoot(b2);

        return new NetWithStringRepresentation(net, "(a=[<nop>], (a, <nop>))");
    }

	public static NetWithStringRepresentation buildNet2() {
    	Net net = new Net();

    	net.runWithMarkerNode(dummy -> {
	        UNode u1 = net.newUNode(dummy);			// a
	        UNode u2 = net.newUNode(dummy);			// b
	        BNode b1 = net.newBNode(dummy, dummy);
	        BNode b2 = net.newBNode(dummy, dummy);	// A
	        BNode b3 = net.newBNode(dummy, dummy);
	        BNode bb = net.newBNode(dummy, dummy);
	        CNode nop = net.newCNode(CommandFactory.NOP_COMMAND);

	        u1.setChild(u2, Purview.DIRECT);
	        u2.setChild(b1, Purview.DIRECT);
	        b1.setLeftChild(bb, Purview.DIRECT);
	        b1.setRightChild(b2, Purview.DIRECT);
	        b2.setLeftChild(bb, Purview.DIRECT);
	        b2.setRightChild(b3, Purview.DIRECT);
	        b3.setLeftChild(bb, Purview.DIRECT);
	        b3.setRightChild(u2, Purview.DIRECT);
	        bb.setLeftChild(nop, Purview.DIRECT);
	        bb.setRightChild(u1, Purview.DIRECT);

	        net.setRoot(u1);
    	});
        return new NetWithStringRepresentation(net, "a=[b=[(A=(<nop>, a), (A, (A, b)))]]");
    }

	public NetWithStringRepresentation buildNetDeep() {
    	Net net = new Net();

        CNode nop = net.newCNode(CommandFactory.NOP_COMMAND);
        Node n = nop;
        for (int i = 0; i < 20; i++) {
        	n = net.newUNode(n);
        }

        net.setRoot(n);

        return new NetWithStringRepresentation(net, "[[[[[[[[[[[[[[[[[[[[<nop>]]]]]]]]]]]]]]]]]]]]");
    }
}
