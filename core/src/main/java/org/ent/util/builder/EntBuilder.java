package org.ent.util.builder;

import org.ent.net.Net;
import org.ent.net.node.Node;
import org.ent.permission.Permissions;

import java.util.ArrayList;
import java.util.List;

public class EntBuilder implements AutoCloseable {

    private final static ThreadLocal<EntBuilder> instance = new ThreadLocal<>();

    List<NodeTemplate> nodeTemplates = new ArrayList<>();

    public EntBuilder() {
        instance.set(this);
    }

    public static NodeTemplate node() {
        NodeTemplate nodeTemplate = new NodeTemplate();
        instance.get().nodeTemplates.add(nodeTemplate);
        return nodeTemplate;
    }

    public static ExternalNode external(Node node) {
        return new ExternalNode(node);
    }

    public static void chain(NodeTemplate... refs) {
        for (int i = 0; i < refs.length - 1; i++) {
            refs[i].right(refs[i + 1]);
        }
    }

    public Net build() {
        Net net = new Net();
        for (NodeTemplate ref : nodeTemplates) {
            Node node = net.newNode(ref.getValue(), Permissions.DIRECT);
            if (ref.isRoot()) {
                net.setRoot(node);
            }
            if (ref.getName() != null) {
                node.setName(ref.getName());
            }
            ref.setResolved(node);
        }
        for (NodeTemplate ref : nodeTemplates) {
            Node node = ref.getResolved();
            Node childLeft = switch (ref.getLeft()) {
                case null -> node;
                case ExternalNode ext -> ext.node();
                case NodeTemplate nt -> nt.getResolved();
            };
            Node childRight = switch (ref.getRight()) {
                case null -> node;
                case ExternalNode ext -> ext.node();
                case NodeTemplate nt -> nt.getResolved();
            };
            node.setLeftChild(childLeft, Permissions.DIRECT);
            node.setRightChild(childRight, Permissions.DIRECT);
        }
        return net;
    }

    @Override
    public void close() {
        instance.remove();
    }
}
