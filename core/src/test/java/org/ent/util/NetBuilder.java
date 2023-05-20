package org.ent.util;

import org.ent.net.Net;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.operation.BiOperation;
import org.ent.net.node.cmd.veto.BiCondition;
import org.ent.net.node.cmd.veto.Vetos;

public class NetBuilder {

    private final static ThreadLocal<Net> currentNet = new ThreadLocal<>();

    private NetBuilder() {
        Net net = new Net();
        currentNet.set(net);
    }

    public static NetBuilder builder() {
        return new NetBuilder();
    }

    public Net net(Node s) {
        Net net = currentNet.get();
        net.setRoot(s);
        currentNet.remove();
        return net;
    }

    public static Node node() {
        return currentNet.get().newNode();
    }

    public static Node node(Command c) {
        return currentNet.get().newCNode(c.getValue());
    }

    public static Node node(int value, Node n1, Node n2) {
        return currentNet.get().newNode(value, n1, n2);
    }

    public static Node node(Command c, Node n1, Node n2) {
        return node(c.getValue(), n1, n2);
    }

    public static Node node(Node n1, Node n2) {
        return node(0, n1, n2);
    }

    public static Node node(BiOperation c, Node n1, Node n2) {
        return node(Commands.get(c), n1, n2);
    }

    public static Node value(BiCondition c) {
        return value(Vetos.get(c).getValue());
    }

    public static Node node(BiCondition c, Node n1, Node n2) {
        return node(Vetos.get(c).getValue(), n1, n2);
    }

    public static Node unary(Node child) {
        return currentNet.get().newUNode(child);
    }

    public static Node value(int v) {
        return currentNet.get().newNode(v);
    }
}
