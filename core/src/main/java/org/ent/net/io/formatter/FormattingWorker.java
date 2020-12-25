package org.ent.net.io.formatter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ent.net.NetController;
import org.ent.net.ReadOnlyNetController;
import org.ent.net.node.BNode;
import org.ent.net.node.CNode;
import org.ent.net.node.MarkerNode;
import org.ent.net.node.Node;
import org.ent.net.node.UNode;

public class FormattingWorker {

	private static final String ELLIPSE = "...";

	private final NetController controller = new ReadOnlyNetController();

    private final List<Node> rootNodes;

    private final Map<Node, String> givenNodeNames;

    private final Map<Node, String> variableBindings;

	private final Integer maxDepth;

	private final StringBuilder stringBuilder;

	private boolean ascii;

	private int uNodeVariableNameIndex;

	private int bNodeVariableNameIndex;

	private int cNodeVariableNameIndex;

	public FormattingWorker(List<Node> rootNodes, Map<Node, String> givenNodeNames, Integer maxDepth) {
		this.rootNodes = rootNodes;
		if (givenNodeNames != null) {
			this.givenNodeNames = givenNodeNames;
		} else {
			this.givenNodeNames = new HashMap<>();
		}
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
        	stringBuilder.append("=");
    		variableBindings.put(node, variableName);
        }

        if (node instanceof BNode bnode) {
			stringBuilder.append("(");
            doFormatRecursively(bnode.getLeftChild(controller), level + 1);
            stringBuilder.append(", ");
            doFormatRecursively(bnode.getRightChild(controller), level + 1);
            stringBuilder.append(")");
        } else if (node instanceof UNode unode) {
			stringBuilder.append("[");
            doFormatRecursively(unode.getChild(controller), level + 1);
            stringBuilder.append("]");
        } else if (node instanceof CNode cnode) {
			stringBuilder.append("<");
			stringBuilder.append(ascii ? cnode.getCommand().getShortNameAscii() : cnode.getCommand().getShortName());
            stringBuilder.append(">");
        } else if (node instanceof MarkerNode) {
        	stringBuilder.append(ascii ? MarkerNode.MARKER_NODE_SYMBOL_ASCII : MarkerNode.MARKER_NODE_SYMBOL);
        }
	}

	private boolean requiresVariable(Node n) {
		if (n instanceof MarkerNode) {
			return false;
		}
		int inverseReferences = n.getHub().getInverseReferences().size();
		if (rootNodes.contains(n)) {
			return inverseReferences >= 1;
		} else {
			return inverseReferences >= 2;
		}
	}

	private String determineVariableName(Node node) {
		String name;
		String givenNodeName = givenNodeNames.get(node);
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
		return givenNodeNames.containsValue(name) || variableBindings.containsValue(name);
	}

	private String getNewVariableName(Node n) {
		int index;
		if (n instanceof UNode) {
			index = uNodeVariableNameIndex;
			uNodeVariableNameIndex++;
		} else if (n instanceof BNode) {
			index = bNodeVariableNameIndex;
			bNodeVariableNameIndex++;
		} else if (n instanceof CNode) {
			index = cNodeVariableNameIndex;
			cNodeVariableNameIndex++;
		} else {
			throw new AssertionError();
		}
		String name = VariableNameHelper.getLetterBasedVariableNameForIndex(index);
		if (n instanceof UNode) {
			return name;
		} else if (n instanceof BNode) {
			return name.toUpperCase();
		} else if (n instanceof CNode) {
			return "_" + name;
		} else {
			throw new AssertionError();
		}
	}

}
