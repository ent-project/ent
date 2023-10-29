package org.ent.util.builder;

import org.ent.net.Net;
import org.ent.net.node.Node;
import org.ent.permission.Permissions;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class EntBuilder {

    Set<NodeTemplate> nodeTemplates = new LinkedHashSet<>();

    public NodeTemplate node() {
        NodeTemplate nodeTemplate = new NodeTemplate();
        nodeTemplates.add(nodeTemplate);
        return nodeTemplate;
    }

    public void chain(NodeTemplate... refs) {
        nodeTemplates.add(refs[0]);
        for (int i = 0; i < refs.length - 1; i++) {
            nodeTemplates.add(refs[i]);
            if (refs[i].isSplit()) {
                NodeProxy right = refs[i].getRight();
                if (right instanceof NodeTemplate templ) {
                    templ.right(refs[i + 1]);
                } else {
                    throw new IllegalArgumentException("Cannot chain split");
                }
            } else {
                refs[i].right(refs[i + 1]);
            }
        }
    }

    public Net build(NodeTemplate nt) {
        nodeTemplates.add(nt);
        return build();
    }

    public Net build() {
        Net net = new Net();

        HashSet<NodeTemplate> nodeTemplatesCopy = new HashSet<>(nodeTemplates);
        for (NodeTemplate ref : nodeTemplatesCopy) {
            collectRec(ref);
        }

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

    private void collectRec(NodeTemplate ref) {
        if (ref.getLeft() instanceof NodeTemplate nt) {
            boolean added = nodeTemplates.add(nt);
            if (added) {
                collectRec(nt);
            }
        }
        if (ref.getRight() instanceof NodeTemplate nt) {
            boolean added = nodeTemplates.add(nt);
            if (added) {
                collectRec(nt);
            }
        }
    }
}
