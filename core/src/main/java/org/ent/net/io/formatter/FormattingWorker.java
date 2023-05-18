package org.ent.net.io.formatter;

import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.MarkerNode;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.CommandFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormattingWorker {

	private static final String ELLIPSE = "...";

	private final Net net;

    private final List<Node> rootNodes;

	private final boolean forceGivenNodeNames;

    private final Map<Node, String> variableBindings;

	private final Integer maxDepth;

	private final StringBuilder stringBuilder;

	private boolean ascii;

	private int uNodeVariableNameIndex;

	private int bNodeVariableNameIndex;

	private int cNodeVariableNameIndex;

	public FormattingWorker(Net net, List<Node> rootNodes, boolean forceGivenNodeNames, Integer maxDepth) {
		this.net = net;
		this.rootNodes = rootNodes;
		this.forceGivenNodeNames = forceGivenNodeNames;
		this.variableBindings = new HashMap<>();
		this.maxDepth = maxDepth;
		this.stringBuilder = new StringBuilder();
	}

	public boolean isAscii() {
		return ascii;
	}

	public void setAscii(boolean ascii) {
		this.ascii = ascii;
	}

	public FormattingWorker withAscii(boolean ascii) {
		setAscii(ascii);
		return this;
	}

	public String formatRecursively() {

        String delimiter = null;
        for (Node node : rootNodes) {
            if (delimiter == null) {
                delimiter = "; ";
            } else {
                stringBuilder.append(delimiter);
            }
            doFormatRecursively(node, 0);
        }

        return stringBuilder.toString();
	}

	public Map<Node, String> getVariableBindings() {
		return variableBindings;
	}

	private void doFormatRecursively(Node node, int level) {
        if (maxDepth != null && level >= maxDepth) {
        	stringBuilder.append(ELLIPSE);
            return;
        }

        String variableName = variableBindings.get(node);
        if (variableName != null) {
        	stringBuilder.append(variableName);
            return;
        }

        if (requiresVariable(node)) {
        	variableName = determineVariableName(node);
        	stringBuilder.append(variableName);
        	stringBuilder.append(":");
    		variableBindings.put(node, variableName);
        }
		if (node.isMarkerNode()) {
			stringBuilder.append(ascii ? MarkerNode.MARKER_NODE_SYMBOL_ASCII : MarkerNode.MARKER_NODE_SYMBOL);
		} else {
			if (node.getValue() != 0 || node.isCommandNode()) {
				Command command = CommandFactory.getByValue(node.getValue());
				if (command != null) {
					stringBuilder.append("<");
					stringBuilder.append(ascii ? command.getShortNameAscii() : command.getShortName());
					stringBuilder.append(">");
				} else {
					stringBuilder.append(String.format("#%x", node.getValue()));
				}
			}
			if (node.isUnaryNode()) {
				stringBuilder.append("[");
				doFormatRecursively(node.getLeftChild(Purview.DIRECT), level + 1);
				stringBuilder.append("]");
			} else if (!node.isCommandNode()) {
				stringBuilder.append("(");
				Node leftChild = node.getLeftChild(Purview.DIRECT);
				doFormatRecursively(leftChild, level + 1);
				stringBuilder.append(", ");
				Node rightChild = node.getRightChild(Purview.DIRECT);
				doFormatRecursively(rightChild, level + 1);
				stringBuilder.append(")");
			}
		}
	}

	private boolean requiresVariable(Node n) {
		if (n.isMarkerNode()) {
			return false;
		}
		if (forceGivenNodeNames && net.getName(n) != null) {
			return true;
		}
		int references = n.getHub().getInverseReferences().size();
		if (n.getRightChild(Purview.DIRECT) == n) {
			references--;
			if (n.getLeftChild(Purview.DIRECT) == n) {
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
		String givenNodeName = net.getName(node);
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
		return net.getByName(name) != null || variableBindings.containsValue(name);
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
