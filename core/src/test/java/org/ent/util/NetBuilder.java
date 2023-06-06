package org.ent.util;

import org.ent.Ent;
import org.ent.net.Net;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.operation.BiOperation;
import org.ent.net.node.cmd.operation.MonoOperation;
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

    public Net net(Node root) {
        Net net = currentNet.get();
        net.setRoot(root);
        currentNet.remove();
        return net;
    }

    public Ent ent(Node root) {
        return new Ent(net(root));
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

    public static Node node(Node node1, Node node2) {
        return node(0, node1, node2);
    }

    public static Node node(BiOperation operation, Node node1, Node node2) {
        return node(Commands.get(operation), node1, node2);
    }

    public static Node value(BiCondition c) {
        return value(Vetos.get(c).getValue());
    }

    public static Node node(BiCondition c, Node n1, Node n2) {
        return node(Vetos.get(c).getValue(), n1, n2);
    }

    public static Node unary(int value, Node child) {
        Node node = currentNet.get().newUNode(child);
        node.setValue(value);
        return node;
    }

    public static Node unary(BiOperation operation, Node node) {
        return unary(Commands.get(operation), node);
    }

    public static Node unary(MonoOperation operation, Node node) {
        return unary(Commands.get(operation), node);
    }

    public static Node unary(Command command, Node child) {
        return unary(command.getValue(), child);
    }

        public static Node unary(Node child) {
        return currentNet.get().newUNode(child);
    }

    public static Node value(int v) {
        return currentNet.get().newNode(v);
    }

    public static Node ignored() {
        return value(0);
    }

}
