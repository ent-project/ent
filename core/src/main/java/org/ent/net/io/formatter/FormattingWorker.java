package org.ent.net.io.formatter;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.ent.Ent;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.node.MarkerNode;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Values;
import org.ent.permission.Permissions;

import java.util.*;

public class FormattingWorker {

    private static final String ELLIPSE = "...";

    private final Ent ent;

    private final List<Node> rootNodes;

    private final boolean forceGivenNodeNames;

    private final Map<Node, String> variableBindings;

    private final Integer maxDepth;

    private final StringBuilder stringBuilder;

    private int uNodeVariableNameIndex;
    private int bNodeVariableNameIndex;
    private int cNodeVariableNameIndex;

    private Multiset<Node> inverseReferences;
    private Set<Node> crossDomainTarget;

    // was the Node defined or just referenced by name?
    private final Set<Node> hasBeenDefined = new HashSet<>();

    public FormattingWorker(Ent ent, List<Node> rootNodes, boolean forceGivenNodeNames, Integer maxDepth) {
        this.ent = ent;
        this.rootNodes = rootNodes;
        this.forceGivenNodeNames = forceGivenNodeNames;
        this.variableBindings = new HashMap<>();
        this.maxDepth = maxDepth;
        this.stringBuilder = new StringBuilder();
    }

    public String formatRecursively() {
        buildInverseReferences();

        for (int i = 0; i < ent.getDomains().size(); i++) {
            Net domain = ent.getDomains().get(i);
            if (i > 0) {
                stringBuilder.append("\n");
                String domainName = domain.getName();
                if (domainName != null) {
                    stringBuilder.append(domainName);
                } else {
                    stringBuilder.append("domain").append(i);
                }
                stringBuilder.append(" { ");
            }

            String delimiter = null;
            for (Node root : rootNodes) {
                if (root.getNet() != domain) {
                    continue;
                }
                if (delimiter == null) {
                    delimiter = "; ";
                } else {
                    stringBuilder.append(delimiter);
                }
                doFormatRecursively(domain, root, 0);
            }

            if (i > 0) {
                stringBuilder.append(" }");
            }
        }
        return stringBuilder.toString();
    }

    private void buildInverseReferences() {
        inverseReferences = HashMultiset.create();
        crossDomainTarget = new HashSet<>();
        for (Net net : ent.getDomains()) {
            for (Node node : net.getNodes()) {
                if (node == null) {
                    continue;
                }
                for (ArrowDirection direction : ArrowDirection.values()) {
                    Node child = node.getChild(direction, Permissions.DIRECT);
                    if (!child.isMarkerNode()) {
                        inverseReferences.add(child);
                        if (node.getNet() != child.getNet()) {
                            crossDomainTarget.add(child);
                        }
                    }
                }
            }
        }
    }

    public Map<Node, String> getVariableBindings() {
        return variableBindings;
    }

    private void doFormatRecursively(Net domain, Node node, int level) {
        if (maxDepth != null && level >= maxDepth) {
            stringBuilder.append(ELLIPSE);
            return;
        }

        if (hasBeenDefined.contains(node)) {
            String variableName = variableBindings.get(node);
            if (variableName == null) {
                throw new AssertionError();
            }
            stringBuilder.append(variableName);
            return;
        }

        if (requiresVariable(node)) {
            String variableName = determineVariableName(node);
            stringBuilder.append(variableName);
            variableBindings.put(node, variableName);
            if (node.getNet() != domain) {
                // will be defined later
                return;
            } else {
                stringBuilder.append(":");
            }
        } else {
            if (node.getNet() != domain) {
                throw new AssertionError();
            }
        }

        if (node.isMarkerNode()) {
            stringBuilder.append(MarkerNode.MARKER_NODE_SYMBOL);
        } else {
            hasBeenDefined.add(node);
            if (node.getValue(Permissions.DIRECT) != 0 || node.isLeafNode()) {
                String name = Values.getName(node.getValue(Permissions.DIRECT));
                if (name != null) {
                    stringBuilder.append("<");
                    stringBuilder.append(name);
                    stringBuilder.append(">");
                } else {
                    stringBuilder.append(String.format("#%x", node.getValue(Permissions.DIRECT)));
                }
            }
            if (node.isUnaryNode()) {
                stringBuilder.append("[");
                doFormatRecursively(domain, node.getLeftChild(Permissions.DIRECT), level + 1);
                stringBuilder.append("]");
            } else if (!node.isLeafNode()) {
                stringBuilder.append("(");
                Node leftChild = node.getLeftChild(Permissions.DIRECT);
                doFormatRecursively(domain, leftChild, level + 1);
                stringBuilder.append(", ");
                Node rightChild = node.getRightChild(Permissions.DIRECT);
                doFormatRecursively(domain, rightChild, level + 1);
                stringBuilder.append(")");
            }
        }
    }

    private boolean requiresVariable(Node n) {
        if (n.isMarkerNode()) {
            return false;
        }
        if (crossDomainTarget.contains(n)) {
            return true;
        }
        if (forceGivenNodeNames && n.getName() != null) {
            return true;
        }
        int references = inverseReferences.count(n);
        if (n.getRightChild(Permissions.DIRECT) == n) {
            references--;
            if (n.getLeftChild(Permissions.DIRECT) == n) {
                references--;
            }
        }
        if (rootNodes.contains(n)) {
            return references >= 1;
        } else {
            return references >= 2;
        }
    }

    private String determineVariableName(Node node) {
        String name;
        String givenNodeName = node.getName();
        if (givenNodeName != null) {
            return givenNodeName;
        } else {
            do {
                name = getNewVariableName(node);
            } while (variableNameIsTaken(name));
        }
        return name;
    }

    private boolean variableNameIsTaken(String name) {
        if (variableBindings.containsValue(name)) {
            return true;
        }
        for (Net domain : ent.getDomains()) {
            if (domain.getByName(name) != null) {
                return true;
            }
        }
        return false;
    }

    private String getNewVariableName(Node n) {
        int index =
                switch (n.getNodeType()) {
                    case UNARY_NODE -> uNodeVariableNameIndex++;
                    case BINARY_NODE -> bNodeVariableNameIndex++;
                    case COMMAND_NODE -> cNodeVariableNameIndex++;
                    case MARKER_NODE -> throw new AssertionError();
                };
        String name = VariableNameHelper.getLetterBasedVariableNameForIndex(index);
        return switch (n.getNodeType()) {
            case UNARY_NODE -> name;
            case BINARY_NODE -> name.toUpperCase();
            case COMMAND_NODE -> "_" + name;
            case MARKER_NODE -> throw new AssertionError();
        };
    }
}
