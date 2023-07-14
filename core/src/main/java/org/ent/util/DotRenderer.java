package org.ent.util;

import org.ent.Ent;
import org.ent.LazyPortalArrow;
import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.BiCommand;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.MonoCommand;
import org.ent.net.node.cmd.TriCommand;
import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.operation.AncestorExchangeNormalOperation;
import org.ent.net.node.cmd.operation.AncestorExchangeOperation;
import org.ent.net.node.cmd.operation.BiOperation;
import org.ent.net.node.cmd.operation.BiValueOperation;
import org.ent.net.node.cmd.operation.DupNormalOperation;
import org.ent.net.node.cmd.operation.DupOperation;
import org.ent.net.node.cmd.operation.SetOperation;
import org.ent.net.node.cmd.veto.Veto;
import org.ent.net.node.cmd.veto.Vetos;

import java.util.LinkedHashSet;
import java.util.Set;

public class DotRenderer {

    private static final String COLOR_PORTAL = "wheat1";
    private static final String COLOR_NODE = "ivory2";
    private final Ent ent;
    private final StringBuilder sb;
    private Node target1, target2, target3;
    private Arrow targetArrow;

    public DotRenderer(Ent ent) {
        this.ent = ent;
        this.sb = new StringBuilder();
    }

    public String render() {
        sb.append("digraph G {\n");
        sb.append(" node[style=filled, color=").append(COLOR_NODE).append("];\n");
        sb.append(" edge[arrowsize=0.6];\n");
        determineCommandTargets();
        Set<Node> nodesConnectedToRoot = new LinkedHashSet<>();
        collectNodesRightToLeft(ent.getNet().getRoot(), nodesConnectedToRoot);
        renderNetNodes(nodesConnectedToRoot, ent.getNet(), "n");
        // portals
        for (int i = 0; i < ent.getPortals().size(); i++) {
            sb.append(" p").append(i).append("[label=\"Portal ").append(i).append("\" shape=invhouse color=").append(COLOR_PORTAL).append("];\n");
        }
        for (int i = 0; i < ent.getDomains().size(); i++) {
            Net domain = ent.getDomains().get(i);
            sb.append(" subgraph cluster_").append(i).append(" {\n");
            sb.append(" color=\"gray\";\n");
            if (domain.getName() != null) {
                sb.append(" label=\"").append(domain.getName()).append("\";\n");
                sb.append(" fontcolor=\"gray\";\n");
                sb.append(" labeljust=\"l\";\n");
            }
            Set<Node> domainNodesToRender = new LinkedHashSet<>();
            collectNodesRightToLeft(domain.getRoot(), domainNodesToRender);
            for (int j = 0; j < ent.getPortals().size(); j++) {
                Arrow portal = ent.getPortals().get(j);
                if (portal instanceof LazyPortalArrow lazyPortalArrow) {
                    if (!lazyPortalArrow.isInitialized()) {
                        continue;
                    }
                }
                Node target = portal.getTarget(Purview.DIRECT);
                if (target.getNet() == domain) {
                    collectNodesRightToLeft(target, domainNodesToRender);
                }
            }
            renderNetNodes(domainNodesToRender, domain, "d"+i+"n");
            sb.append(" }\n");
        }
        // edges for portals
        for (int i = 0; i < ent.getPortals().size(); i++) {
            Arrow portal = ent.getPortals().get(i);
            if (portal instanceof LazyPortalArrow lazyPortalArrow) {
                if (!lazyPortalArrow.isInitialized()) {
                    continue;
                }
            }
            Node target = portal.getTarget(Purview.DIRECT);
            sb.append(" p").append(i).append(" -> ");
            int domainIndex = getDomainIndex(target);
            sb.append("d").append(domainIndex).append("n").append(target.getIndex()).append(" [style=dotted]");
            if (portal == targetArrow) {
                sb.append("[penwidth=6]");
            }
            sb.append(";\n");
        }

        sb.append("}\n");
        String string = sb.toString();
        System.err.println(string);
        return string;
    }

    private void determineCommandTargets() {
        Node root = ent.getNet().getRoot();
        Command command = Commands.getByValue(root.getValue(Purview.DIRECT));
        if (command != null) {
            if (command instanceof MonoCommand monoCommand) {
                Accessor accessor = monoCommand.getAccessor();
                target1 = accessor.getTarget(root, ent, Purview.DIRECT);
            } else if (command instanceof BiCommand biCommand) {
                Accessor accessor1 = biCommand.getAccessor1();
                Accessor accessor2 = biCommand.getAccessor2();
                BiOperation operation = biCommand.getOperation();
                if (operation instanceof BiValueOperation || operation instanceof AncestorExchangeOperation || operation instanceof AncestorExchangeNormalOperation) {
                    target1 = accessor1.getTarget(root, ent, Purview.DIRECT);
                    target2 = accessor2.getTarget(root, ent, Purview.DIRECT);
                } else if (operation instanceof SetOperation || operation instanceof DupOperation || operation instanceof DupNormalOperation) {
                    targetArrow = accessor1.get(root, ent, Purview.DIRECT);
                    target2 = accessor2.getTarget(root, ent, Purview.DIRECT);
                }
            } else if (command instanceof TriCommand triCommand) {
                Accessor accessor1 = triCommand.getAccessor1();
                Accessor accessor2 = triCommand.getAccessor2();
                Accessor accessor3 = triCommand.getAccessor3();
                target1 = accessor1.getTarget(root, ent, Purview.DIRECT);
                target2 = accessor2.getTarget(root, ent, Purview.DIRECT);
                target3 = accessor3.getTarget(root, ent, Purview.DIRECT);
            }
        }
    }

    private int getDomainIndex(Node target) {
        for (int j = 0; j < ent.getDomains().size(); j++) {
            if (ent.getDomains().get(j) == target.getNet()) {
                return j;
            }
        }
        throw new AssertionError();
    }

    private void renderNetNodes(Set<Node> nodes, Net net, String nodePrefix) {
        for (Node node : nodes) {
            String nodeName = renderNode(net, nodePrefix, node);
            for (Arrow arrow : node.getArrows()) {
                Node child = arrow.getTarget(Purview.DIRECT);
                if (node == child) {
                    continue;
                }
                sb.append(" \"").append(nodeName).append("\":");
                sb.append(switch (arrow.getDirection()) {
                    case LEFT -> "l";
                    case RIGHT -> "r";
                });
                sb.append(" -> ").append(nodePrefix).append(child.getIndex());
                sb.append(switch (arrow.getDirection()) {
                    case LEFT -> " [color=hotpink4]";
                    case RIGHT -> " [color=olivedrab4]";
                });
                if (arrow == targetArrow) {
                    sb.append("[penwidth=4]");
                }
                sb.append(";\n");
            }
        }
    }

    private String renderNode(Net net, String nodePrefix, Node node) {
        String nodeName = nodePrefix + node.getIndex();
        sb.append(" ").append(nodeName);
        int value = node.getValue(Purview.DIRECT);
        if (value == 0) {
            String size = isTarget(node) ? "0.2" : "0.1";
            String color = getOutlineColor(node);
            sb.append("[shape=Mrecord][label=\"<l>|<r>\", width=%s, height=%s, fixedsize=true, style=filled, color=%s]"
                    .formatted(size, size, color));
        } else {
            Command command = Commands.getByValue(value);
            if (command != null) {
                if (isTarget(node)) {
                   sb.append("[shape=plain style=\"\" label=");
                   sb.append("<<table border='4' color='%s' cellborder='0' style='rounded' cellspacing='5' cellpadding='0' bgcolor='%s'>".formatted(getOutlineColor(node), getFillColor(command)));
                   sb.append("<tr><td port='l'></td>");
                   sb.append("<td>%s</td>".formatted(escape(command.getShortName())));
                   sb.append("<td port='r'></td></tr></table>>]");
                } else {
                    sb.append("[shape=Mrecord][label=\"<l>|").append(escape(command.getShortName())).append("|<r>\"]");
                    String color = getFillColor(command);
                    if (color != COLOR_NODE) {
                        sb.append("[color=%s]".formatted(color));
                    }
               }
            } else {
                Veto veto = Vetos.getByValue(value);
                if (veto != null) {
                    sb.append("[shape=record][label=\"<l>|").append(escape(veto.getShortName())).append("|<r>\"][color=beige]");
                } else {
                    String hexString = Integer.toHexString(value);
                    Integer portalIndexLeft = getPortalIndex(value, ArrowDirection.LEFT);
                    Integer portalIndexRight = getPortalIndex(value, ArrowDirection.RIGHT);
                    if (portalIndexLeft != null || portalIndexRight != null) {
                        // render html table
                        sb.append("[shape=plain label=<<table border=\"0\" cellspacing=\"0\" cellpadding=\"11\"><tr>");
                        if (portalIndexLeft != null) {
                            sb.append("<td port=\"l\" cellpadding=\"5\" bgcolor=\"").append(COLOR_PORTAL).append("\">P").append(portalIndexLeft).append("</td>");
                        } else {
                            sb.append("<td port=\"l\" cellpadding=\"0\" bgcolor=\"").append(COLOR_NODE).append("\">").append("</td>");
                        }
                        sb.append("<td bgcolor=\"").append(COLOR_NODE).append("\">").append(hexString).append("</td>");
                        if (portalIndexRight != null) {
                            sb.append("<td port=\"r\" cellpadding=\"5\" bgcolor=\"").append(COLOR_PORTAL).append("\">P").append(portalIndexRight).append("</td>");
                        } else {
                            sb.append("<td port=\"r\" cellpadding=\"0\" bgcolor=\"").append(COLOR_NODE).append("\">").append("</td>");
                        }
                        sb.append("</tr></table>>]");
                    } else {
                        if (node.isLeafNode()) {
                            sb.append("[shape=circle, label=\"").append(hexString).append("\"]");
                            if (isTarget(node)) {
                                sb.append("[color=%s fillcolor=%s penwidth=4]".formatted(getOutlineColor(node), COLOR_NODE));
                            }
                        } else {
                            sb.append("[shape=record][label=\"<l>|").append(hexString).append("|<r>\"]");
                        }
                    }
                }
            }
        }
        if (node == net.getRoot()) {
            sb.append("[color=slategray1]");
        }
        sb.append(";\n");
        return nodeName;
    }

    private String getOutlineColor(Node node) {
        if (node == target1) {
            return "black";
        } else if (node == target2) {
            return "darkblue";
        } else if (node == target3) {
            return "palevioletred4";
        }
        return "black";
    }

    private String getFillColor(Command command) {
        if (command.getValue() == Commands.FINAL_SUCCESS.getValue()) {
            return "palegreen";
        } else if (command.getValue() == Commands.FINAL_FAILURE.getValue()) {
            return "mistyrose2";
        }
        return COLOR_NODE;
    }

    private boolean isTarget(Node node) {
        return node == target1 || node == target2 || node == target3;
    }

    private Integer getPortalIndex(int value, ArrowDirection dir) {
        int portalIndex = Ent.getPortalIndex(value, dir);
        if (ent.isPortal(portalIndex)) {
            return portalIndex;
        } else {
            return null;
        }
    }

    private static void collectNodesRightToLeft(Node node, Set<Node> collected) {
        if (!collected.add(node)) {
            return;
        }
        collectNodesRightToLeft(node.getRightChild(Purview.DIRECT), collected);
        collectNodesRightToLeft(node.getLeftChild(Purview.DIRECT), collected);
    }

    private static String escape(String shortName) {
        return shortName.replaceAll("<", "&lt;")
                .replaceAll("[\\\\]", "\\\\\\\\");
    }
}
