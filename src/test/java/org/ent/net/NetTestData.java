package org.ent.net;

import java.util.Arrays;
import java.util.List;

import org.ent.net.node.BNode;
import org.ent.net.node.CNode;
import org.ent.net.node.MarkerNode;
import org.ent.net.node.Node;
import org.ent.net.node.UNode;
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

	public NetWithStringRepresentation buildNet0() {
    	Net net = new Net();
    	NetController controller = new DefaultNetController(net);
        Node dummy = new MarkerNode();

        BNode b1 = controller.newBNode(dummy, dummy);
        UNode u1 = controller.newUNode(dummy);
        CNode nop = controller.newCNode(new NopCommand());

        b1.setLeftChild(controller, u1);
        b1.setRightChild(controller, nop);
        u1.setChild(controller, u1);

        net.setRoot(b1);

        return new NetWithStringRepresentation(net, "(a=[a], <nop>)");
    }

	public NetWithStringRepresentation buildNet1() {
    	Net net = new Net();
    	NetController controller = new DefaultNetController(net);

        UNode a = controller.newUNode(controller.newCNode(new NopCommand()));
        BNode b1 = controller.newBNode(a, controller.newCNode(new NopCommand()));
        BNode b2 = controller.newBNode(a, b1);

        net.setRoot(b2);

        return new NetWithStringRepresentation(net, "(a=[<nop>], (a, <nop>))");
    }

	public NetWithStringRepresentation buildNet2() {
    	Net net = new Net();
    	NetController controller = new DefaultNetController(net);
    	Node dummy = new MarkerNode();

        UNode u1 = controller.newUNode(dummy);			// a
        UNode u2 = controller.newUNode(dummy);			// b
        BNode b1 = controller.newBNode(dummy, dummy);
        BNode b2 = controller.newBNode(dummy, dummy);	// A
        BNode b3 = controller.newBNode(dummy, dummy);
        BNode bb = controller.newBNode(dummy, dummy);
        CNode nop = controller.newCNode(new NopCommand());

        u1.setChild(controller, u2);
        u2.setChild(controller, b1);
        b1.setLeftChild(controller, bb);
        b1.setRightChild(controller, b2);
        b2.setLeftChild(controller, bb);
        b2.setRightChild(controller, b3);
        b3.setLeftChild(controller, bb);
        b3.setRightChild(controller, u2);
        bb.setLeftChild(controller, nop);
        bb.setRightChild(controller, u1);

        net.setRoot(u1);

        return new NetWithStringRepresentation(net, "a=[b=[(A=(<nop>, a), (A, (A, b)))]]");
    }

	public NetWithStringRepresentation buildNetDeep() {
    	Net net = new Net();
    	NetController controller = new DefaultNetController(net);

        CNode nop = controller.newCNode(new NopCommand());
        Node n = nop;
        for (int i = 0; i < 20; i++) {
        	n = controller.newUNode(n);
        }

        net.setRoot(n);

        return new NetWithStringRepresentation(net, "[[[[[[[[[[[[[[[[[[[[<nop>]]]]]]]]]]]]]]]]]]]]");
    }
}
