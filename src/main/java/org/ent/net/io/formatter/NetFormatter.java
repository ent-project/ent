package org.ent.net.io.formatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ent.net.Arrow;
import org.ent.net.Net;
import org.ent.net.NetController;
import org.ent.net.ReadOnlyNetController;
import org.ent.net.node.Node;

public class NetFormatter {

	private final NetController controller = new ReadOnlyNetController();

    private Map<Node, String> givenNodeNames = new HashMap<>();

	private Integer maxDepth;

	private boolean ascii;

	public Integer getMaxDepth() {
		return maxDepth;
	}

	public void setMaxDepth(Integer maxDepth) {
		this.maxDepth = maxDepth;
	}

	public void setNodeNames(Map<Node, String> nodeNames) {
		givenNodeNames.putAll(nodeNames);
	}

	public boolean isAscii() {
		return ascii;
	}

	public void setAscii(boolean ascii) {
		this.ascii = ascii;
	}

	public String format(Net net) {
        Set<Node> collected = new HashSet<>();
        List<Node> rootNodes = new ArrayList<>();
        rootNodes.add(net.getRoot());

        collectRecursively(net.getRoot(), collected);

        Set<Node> missing = new HashSet<>(net.getNodes());
        missing.removeAll(collected);

        while (!missing.isEmpty()) {
            Node nextRoot = missing.iterator().next();
            collectRecursivelyInverted(nextRoot, missing);
            rootNodes.add(nextRoot);
        }

        FormattingWorker worker = new FormattingWorker(rootNodes, givenNodeNames, maxDepth);
        worker.setAscii(ascii);

        String result = worker.formatRecursively();

        givenNodeNames.putAll(worker.getVariableBindings());

        return result;
	}

	private void collectRecursively(Node node, Set<Node> collected) {
        if (collected.contains(node))
            return;
        collected.add(node);
        for (Arrow arrow : node.getArrows()) {
        	Node child = arrow.getTarget(controller);
        	collectRecursively(child, collected);
        }
    }

	private void collectRecursivelyInverted(Node node, Set<Node> missing) {
        if (!missing.contains(node))
            return;
        missing.remove(node);
        for (Arrow arrow : node.getArrows()) {
        	Node child = arrow.getTarget(controller);
        	collectRecursivelyInverted(child, missing);
        }
    }

}
