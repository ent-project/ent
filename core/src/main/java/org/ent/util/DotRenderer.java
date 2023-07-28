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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DotRenderer {

    private static final String COLOR_PORTAL = "wheat1";
    private static final String COLOR_NODE = "ivory2";
    public static final String COLOR_VETO = "beige";
    public static final String COLOR_DOT = "lightcyan4";
    public static final String COLOR_TARGET1 = "black";
    public static final String COLOR_TARGET2 = "darkblue";
    public static final String COLOR_TARGET3 = "palevioletred4";
    private static final int TARGET_HIGHLIGHT_PENWIDTH = 4;
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
        sb.append(" node[style=filled color=%s height=0 width=0];\n"
                .formatted(COLOR_NODE));
        sb.append(" edge[arrowsize=0.6];\n");
        determineCommandTargets();
        Set<Node> nodesConnectedToRoot = new LinkedHashSet<>();
        collectNodesRightToLeft(ent.getNet().getRoot(), nodesConnectedToRoot);
        renderNetNodes(nodesConnectedToRoot, ent.getNet(), "n");
        // portals
        for (int i = 0; i < ent.getPortals().size(); i++) {
            sb.append(" p%s[label=\"Portal %s\" shape=invhouse color=%s height=0.5];\n"
                    .formatted(i, i, COLOR_PORTAL));
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
            renderNetNodes(domainNodesToRender, domain, "d" + i + "n");
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
        return sb.toString();
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
            if (isTarget(node)) {
                TableBuilder table = new TableBuilder();
                table.textCenter = "";
                table.fillColor = COLOR_DOT;
                table.rounded = true;
                table.vanishEmptySides = true;
                table.cellpaddingTable = 0;
                table.cellpaddingCenter = 4;
                table.outlineColors = getOutlineColors(node);
                table.renderTable(sb);
            } else {
                sb.append("[shape=Mrecord label=\"<l>|<r>\" width=0.1 height=0.1 fixedsize=true color=%s]"
                        .formatted(COLOR_DOT));
            }
        } else {
            Command command = Commands.getByValue(value);
            if (command != null) {
                if (isTarget(node)) {
                    TableBuilder table = new TableBuilder();
                    table.textCenter = escape(command.getShortName());
                    table.outlineColors = getOutlineColors(node);
                    table.rounded = true;
                    table.fillColor = getCommandFillColor(command);
                    table.renderTable(sb);
                } else {
                    sb.append("[shape=Mrecord label=\"<l>|%s|<r>\"".formatted(escape(command.getShortName())));
                    String color = getCommandFillColor(command);
                    if (!COLOR_NODE.equals(color)) {
                        sb.append(" color=%s".formatted(color));
                    }
                    sb.append("]");
                }
            } else {
                Veto veto = Vetos.getByValue(value);
                if (veto != null) {
                    if (isTarget(node)) {
                        TableBuilder table = new TableBuilder();
                        table.fillColor = COLOR_VETO;
                        table.textCenter = escape(veto.getShortName());
                        table.rounded = false;
                        table.outlineColors = getOutlineColors(node);
                        table.renderTable(sb);
                    } else {
                        sb.append("[shape=record][label=\"<l>|%s|<r>\"][color=%s]"
                                .formatted(escape(veto.getShortName()), COLOR_VETO));
                    }
                } else {
                    String hexString = Integer.toHexString(value);
                    Integer portalIndexLeft = getPortalIndex(value, ArrowDirection.LEFT);
                    Integer portalIndexRight = getPortalIndex(value, ArrowDirection.RIGHT);
                    if (portalIndexLeft != null || portalIndexRight != null) {
                        TableBuilder table = new TableBuilder();
                        table.rounded = false;
                        table.vanishEmptySides = true;
                        table.outlineColors = getOutlineColors(node);
                        table.fillColor = COLOR_NODE;
                        if (portalIndexLeft != null) {
                            table.textLeft = "P" + portalIndexLeft;
                            table.fillColorLeft = COLOR_PORTAL;
                        } else {
                            table.fillColorLeft = COLOR_NODE;
                        }
                        if (portalIndexRight != null) {
                            table.textRight = "P" + portalIndexRight;
                            table.fillColorRight = COLOR_PORTAL;
                        } else {
                            table.fillColorRight = COLOR_NODE;
                        }
                        table.textCenter = hexString;
                        table.renderTable(sb);
                    } else {
                        if (node.isLeafNode()) {
                            List<String> outlineColors = getOutlineColors(node);
                            if (outlineColors.size() <= 1) {
                                sb.append("[shape=rect style=\"filled,rounded\" label=\"%s\" fillcolor=%s margin=0.07 height=0 width=0"
                                        .formatted(hexString, COLOR_NODE));
                                if (outlineColors.size() > 0) {
                                    sb.append(" color=%s penwidth=%s"
                                            .formatted(getOutlineColors(node).get(0), TARGET_HIGHLIGHT_PENWIDTH));
                                }
                                sb.append("]");
                            } else {
                                TableBuilder table = new TableBuilder();
                                table.textCenter = hexString;
                                table.fillColor = COLOR_NODE;
                                table.rounded = true;
                                table.outlineColors = outlineColors;
                                table.skipSides = true;
                                table.renderTable(sb);
                            }
                        } else {
                            sb.append("[shape=record][label=\"<l>|").append(hexString).append("|<r>\"]");
                        }
                    }
                }
            }
        }
        if (node == net.getRoot()) {
            sb.append("[xlabel=\"⤷\"]");
        }
        sb.append(";\n");
        return nodeName;
    }

    private static class TableBuilder {
        boolean rounded;
        String textCenter;
        String textLeft;
        String textRight;
        String fillColor;
        String fillColorLeft;
        String fillColorRight;
        List<String> outlineColors;
        boolean skipSides;
        Integer cellpaddingTable;
        Integer cellpaddingCenter;
        boolean vanishEmptySides;

        private Boolean consistentColor;
        private boolean paddedSides;

        void renderTable(StringBuilder sb) {
            consistentColor = fillColorLeft == null && fillColorRight == null;
            String mainOutlineColor = outlineColors != null && outlineColors.size() > 0 ? outlineColors.get(0) : null;
            paddedSides = !vanishEmptySides && textLeft == null && textRight == null;
            if (cellpaddingTable == null) {
                cellpaddingTable = 4;
            }

            sb.append("[shape=plain style=\"\" label=<");
            if (outlineColors != null) {
                borderStart(sb);
            }
            sb.append("<table cellspacing=\"0\"");
            if (mainOutlineColor != null) {
                sb.append(" border=\"%s\" color=\"%s\" cellborder=\"0\""
                        .formatted(TARGET_HIGHLIGHT_PENWIDTH, mainOutlineColor));
            } else {
                sb.append(" border=\"0\"");
            }
            if (consistentColor) {
                sb.append(" bgcolor=\"%s\"".formatted(fillColor));
            }
            if (rounded) {
                sb.append(" style=\"rounded\"");
            }
            sb.append(" cellpadding=\"%s\"".formatted(cellpaddingTable));
            sb.append(">");
            sb.append("<tr>");
            renderSideCell(sb, "l", textLeft, fillColorLeft);
            renderCenterCell(sb);
            renderSideCell(sb, "r", textRight, fillColorRight);
            sb.append("</tr></table>");
            if (outlineColors != null) {
                borderEnd(sb);
            }
            sb.append(">]");
        }

        private void renderCenterCell(StringBuilder sb) {
            sb.append("<td");
            if (!consistentColor) {
                sb.append(" bgcolor=\"%s\"".formatted(fillColor));
            }
            if (cellpaddingCenter != null) {
                sb.append(" cellpadding=\"%s\"".formatted(cellpaddingCenter));
            }
            sb.append(">%s</td>".formatted(textCenter));
        }

        private void renderSideCell(StringBuilder sb, String port, String text, String fillColorSide) {
            if (skipSides) {
                return;
            }
            sb.append("<td port=\"%s\"".formatted(port));
            if (text != null) {
                sb.append(" cellpadding=\"5\"");
            } else if (vanishEmptySides) {
                sb.append(" cellpadding=\"0\"");
            }
            if (!consistentColor) {
                sb.append(" bgcolor=\"%s\"".formatted(fillColorSide));
            }
            sb.append(">");
            if (text != null) {
                sb.append(text);
            } else if (paddedSides) {
                sb.append("   ");
            }
            sb.append("</td>");
        }

        private void borderStart(StringBuilder sb) {
            for (int i = outlineColors.size() - 1; i > 0; i--) {
                sb.append("<table border=\"%s\" color=\"%s\" cellborder=\"0\" cellspacing=\"0\""
                        .formatted(TARGET_HIGHLIGHT_PENWIDTH, outlineColors.get(i)));
                if (rounded) {
                    sb.append(" style=\"rounded\"");
                }
                sb.append("><tr><td>");
            }
        }

        private void borderEnd(StringBuilder sb) {
            for (int i = outlineColors.size() - 1; i > 0; i--) {
                sb.append("</td></tr></table>");
            }
        }
    }

    private List<String> getOutlineColors(Node node) {
        ArrayList<String> result = new ArrayList<>();
        if (node == target1) {
            result.add(COLOR_TARGET1);
        }
        if (node == target2) {
            result.add(COLOR_TARGET2);
        }
        if (node == target3) {
            result.add(COLOR_TARGET3);
        }
        return result;
    }

    private String getCommandFillColor(Command command) {
        if (command.getValue() == Commands.FINAL_SUCCESS.getValue()) {
            return "\"#daffb5\"";
        } else if (command.getValue() == Commands.FINAL_FAILURE.getValue()) {
            return "\"#ffcfb5\"";
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
                .replaceAll("[\\\\]", "\\\\\\\\")
                .replaceAll("[|]", "\\\\|");
    }
}
