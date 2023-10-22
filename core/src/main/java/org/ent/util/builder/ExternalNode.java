package org.ent.util.builder;

import org.ent.net.node.Node;

public record ExternalNode(Node node) implements NodeProxy {
    public static ExternalNode external(Node node) {
        return new ExternalNode(node);
    }
}
