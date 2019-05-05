package org.ent.net.io.parser;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ent.net.Arrow;
import org.ent.net.DefaultNetController;
import org.ent.net.Net;
import org.ent.net.NetController;
import org.ent.net.node.MarkerNode;
import org.ent.net.node.Node;

/**
 * A parser that creates a collection of nodes from a text-description in a domain-specific language.
 * <p>
 * In the net DSL, a binary node is represented as "(X1,X2)", where X1 and X2 are the child nodes.
 * A unary node is written as "[X]" with child node X. A command is given as "&lt;<i>cmdname</i>&gt;".
 * A node can be prefixed by "<i>ident</i>=", which introduces a variable name for the given node.
 * The identifier can then be used anywhere in place of a node.
 * <p>
 * Multiple node descriptions may be concatenated, separated by a semicolon.
 * <p>
 * Examples:
 * <ul>
 * <li><code>([&lt;nop&gt;], (&lt;nop&gt;, &lt;nop&gt;))</code>
 * <li><code>B=(u=[B],(&lt;nop&gt;, u))</code>
 * <li><code>(K, K); K=[K]</code>
 * </ul>
 */
public class NetParser {

    private final Node placeholderNode = new MarkerNode();

    private Map<String, Node> nodeNames;

    private final Set<Node> allNodes = new HashSet<>();

    private final List<Node> mainNodes = new ArrayList<>(); // top-level nodes, one for each semicolon-separated expression

	private final Map<NodeTemplate, Node> templateNodeMap = new HashMap<>();

	private FirstPassNetParser firstPassNetParser;

	private boolean markerNodePermitted;

	private MarkerNode markerNode;

    public Net parse(String input) throws ParserException {
        return parse(new StringReader(input));
    }

    public Net parse(Reader reader) throws ParserException {
        clear();
        firstPassNetParser = new FirstPassNetParser(reader);
        if (markerNodePermitted) {
        	firstPassNetParser.setMarkerNodesPermitted(markerNode);
        }
        List<NodeTemplate> mainNodeTemplates = firstPassNetParser.parseAll();
        return buildNet(mainNodeTemplates);
    }

    // values are not unique, i.e. one node can have several names
    public Map<String, Node> getNodeNames() throws ParserException {
        if (nodeNames == null) {
        	nodeNames = buildNodeNames();
        }
        return nodeNames;
    }

    public List<Node> getMainNodes() {
		return mainNodes;
	}

    public NetParser permitMarkerNodes(MarkerNode markerNode) {
    	this.markerNodePermitted = true;
    	this.markerNode = markerNode;
    	return this;
    }

    private Net buildNet(List<NodeTemplate> mainNodeTemplates) throws ParserException {

    	Net net = new Net();
    	if (markerNodePermitted) {
    		net.permitMarkerNode(markerNode);
    	}
        NetController controller = new DefaultNetController(net);

        instantiateNodesFromTemplates(controller);

        fillInChildNodes(controller);

        resolveMainNodes(mainNodeTemplates);

        net.setRoot(mainNodes.get(0));

        net.consistencyTest();
        return net;
    }

	private void instantiateNodesFromTemplates(NetController controller) throws ParserException {
		for (NodeTemplate template : firstPassNetParser.getAllEntries()) {
        	if (!(template instanceof IdentifierNodeTemplate)) {
        		Node node = template.generateNode(controller);
        		templateNodeMap.put(template, node);
        	}
        }
	}

	private void fillInChildNodes(NetController controller) throws ParserException {
		for (NodeTemplate template : firstPassNetParser.getAllEntries()) {
            if (template instanceof IdentifierNodeTemplate) {
                continue;
            }
            Node node = templateNodeMap.get(template);
            for (Arrow arrow : node.getArrows()) {
            	Node child = resolveNode(template.getChild(arrow.getDirection()));
            	if (child == null) {
            		throw new ParserException();
            	}
            	arrow.setTarget(controller, child);
            }
            allNodes.add(node);
        }
	}

	private void resolveMainNodes(List<NodeTemplate> mainNodeTemplates) throws ParserException {
		for (NodeTemplate template : mainNodeTemplates) {
			Node node = resolveNode(template);
			if (node instanceof MarkerNode) {
				throw new ParserException("Top level node must not be a marker node");
			}
			mainNodes.add(node);
		}
	}

	private Map<String, Node> buildNodeNames() throws ParserException {
		Map<String, Node> result = new HashMap<>();
		Map<String, NodeTemplate> identifierMapping = firstPassNetParser.getIdentifierMapping();
		for (Entry<String, NodeTemplate> entry : identifierMapping.entrySet()) {
			String name = entry.getKey();
			NodeTemplate template = entry.getValue();
			Node node = resolveNode(template);
	        result.put(name, node);
		}
		return result;
	}

	private Node resolveNode(NodeTemplate template) throws ParserException {
		NodeTemplate resolvedTemplate;
		if (template instanceof IdentifierNodeTemplate) {
			resolvedTemplate = firstPassNetParser.resolveIdentifier((IdentifierNodeTemplate) template);
		} else {
			resolvedTemplate = template;
		}
		Node resolvedNode = templateNodeMap.get(resolvedTemplate);
		if (resolvedNode == null) {
			throw new ParserException("Unable to get Node for NodeTemplate");
		}
		return resolvedNode;
	}

	private void clear() {
        nodeNames = null;
        allNodes.clear();
        mainNodes.clear();
        templateNodeMap.clear();
    }

}
