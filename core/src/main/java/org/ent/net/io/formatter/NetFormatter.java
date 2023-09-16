package org.ent.net.io.formatter;

import org.ent.Ent;
import org.ent.net.Arrow;
import org.ent.net.Net;
import org.ent.net.node.MarkerNode;
import org.ent.net.node.Node;
import org.ent.permission.Permissions;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class NetFormatter {

	private boolean forceGivenNodeNames;

	private boolean includeOrphans;

	private Integer maxDepth;

	public Integer getMaxDepth() {
		return maxDepth;
	}

	public void setMaxDepth(Integer maxDepth) {
		this.maxDepth = maxDepth;
	}

	public void setForceGivenNodeNames(boolean forceGivenNodeNames) {
		this.forceGivenNodeNames = forceGivenNodeNames;
	}

	public NetFormatter withForceGivenNodeNames(boolean forceGivenNodeNames) {
		setForceGivenNodeNames(forceGivenNodeNames);
		return this;
	}

	public void setIncludeOrphans(boolean includeOrphans) {
		this.includeOrphans = includeOrphans;
	}

	public NetFormatter withIncludeOrphans(boolean includeOrphans) {
		setIncludeOrphans(includeOrphans);
		return this;
	}

	public String format(Ent ent) {
		return format(ent.getNet());
	}

	public String format(@NotNull Net net) {
        Set<Node> collected = new LinkedHashSet<>();
        List<Node> rootNodes = new ArrayList<>();
        rootNodes.add(net.getRoot());

        collectRecursively(net.getRoot(), collected, net.isMarkerNodePermitted());

		if (includeOrphans) {
			Set<Node> missing = new LinkedHashSet<>(net.getNodes());
			missing.removeAll(collected);

			while (!missing.isEmpty()) {
				Node nextRoot = missing.iterator().next();
				collectRecursivelyInverted(nextRoot, missing);
				rootNodes.add(nextRoot);
			}
		}

        FormattingWorker worker = new FormattingWorker(net, rootNodes, forceGivenNodeNames, maxDepth);

        String result = worker.formatRecursively();

		worker.getVariableBindings().forEach(net::setName);

        return result;
	}

	private void collectRecursively(Node node, Set<Node> collected, boolean markerNodesPermitted) {
        if (collected.contains(node))
            return;
        collected.add(node);
        if (node instanceof MarkerNode) {
        	if (markerNodesPermitted) {
        		return;
        	} else {
        		throw new IllegalArgumentException("Found marker node");
        	}
        }
        for (Arrow arrow : node.getArrows()) {
        	Node child = arrow.getTarget(Permissions.DIRECT);
        	collectRecursively(child, collected, markerNodesPermitted);
        }
    }

	private void collectRecursivelyInverted(Node node, Set<Node> missing) {
        if (!missing.contains(node))
            return;
        missing.remove(node);
        for (Arrow arrow : node.getArrows()) {
        	Node child = arrow.getTarget(Permissions.DIRECT);
        	collectRecursivelyInverted(child, missing);
        }
    }

}
