package org.ent.util;

import org.apache.commons.text.StringEscapeUtils;
import org.ent.Ent;
import org.ent.net.Arrow;
import org.ent.net.Net;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.*;
import org.ent.net.node.cmd.accessor.Accessor;
import org.ent.net.node.cmd.operation.*;
import org.ent.net.node.cmd.split.Split;
import org.ent.net.node.cmd.split.Splits;
import org.ent.permission.Permissions;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DotRenderer {

    private static final String COLOR_NODE = "ivory2";
    public static final String COLOR_SPLIT = "beige";
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
        Set<Node> allNodesConnectedToAnyRoot = new LinkedHashSet<>();
        for (Net domain : ent.getDomains()) {
            collectNodesRightToLeft(domain.getRoot(), allNodesConnectedToAnyRoot);
        }
        renderNetNodes(allNodesConnectedToAnyRoot, ent.getNet());
        for (int i = 1; i < ent.getDomains().size(); i++) {
            Net domain = ent.getDomains().get(i);
            sb.append(" subgraph cluster_").append(i).append(" {\n");
            sb.append(" color=\"gray\";\n");
            if (domain.getName() != null) {
                sb.append(" label=\"").append(domain.getName()).append("\";\n");
                sb.append(" fontcolor=\"gray\";\n");
                sb.append(" labeljust=\"l\";\n");
            }
            renderNetNodes(allNodesConnectedToAnyRoot, domain);
            sb.append(" }\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private void determineCommandTargets() {
        Node root = ent.getNet().getRoot();
        Command command = Commands.getByValue(root.getValue(Permissions.DIRECT));
        if (command != null) {
            if (command instanceof MonoCommand monoCommand) {
                Accessor accessor = monoCommand.getAccessor();
                target1 = accessor.getTarget(root, Permissions.DIRECT);
            } else if (command instanceof BiCommand biCommand) {
                Accessor accessor1 = biCommand.getAccessor1();
                Accessor accessor2 = biCommand.getAccessor2();
                BiOperation operation = biCommand.getOperation();
                if (operation instanceof BiValueOperation || operation instanceof AncestorExchangeOperation || operation instanceof AncestorExchangeNormalOperation) {
                    target1 = accessor1.getTarget(root, Permissions.DIRECT);
                    target2 = accessor2.getTarget(root, Permissions.DIRECT);
                } else if (operation instanceof SetOperation || operation instanceof DupOperation || operation instanceof DupNormalOperation) {
                    targetArrow = accessor1.get(root, Permissions.DIRECT);
                    target2 = accessor2.getTarget(root, Permissions.DIRECT);
                }
            } else if (command instanceof TriCommand triCommand) {
                Accessor accessor1 = triCommand.getAccessor1();
                Accessor accessor2 = triCommand.getAccessor2();
                Accessor accessor3 = triCommand.getAccessor3();
                target1 = accessor1.getTarget(root, Permissions.DIRECT);
                target2 = accessor2.getTarget(root, Permissions.DIRECT);
                target3 = accessor3.getTarget(root, Permissions.DIRECT);
            }
        }
    }

    private void renderNetNodes(Set<Node> nodes, Net net) {
        for (Node node : nodes) {
            if (node.getNet() != net) {
                continue;
            }
            String nodeName = renderNode(node);
            for (Arrow arrow : node.getArrows()) {
                Node child = arrow.getTarget(Permissions.DIRECT);
                if (node == child && arrow != targetArrow) {
                    continue;
                }
                sb.append(" \"%s\":".formatted(nodeName));
                sb.append(switch (arrow.getDirection()) {
                    case LEFT -> "l";
                    case RIGHT -> "r";
                });
                sb.append(" -> ").append(getNodeId(child));
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

    private String getNodeId(Node node) {
        int netIndex = node.getNet().getNetIndex();
        if (netIndex <= 0) {
            return "n%s".formatted(node.getIndex());
        } else {
            return "d%sn%s".formatted(netIndex, node.getIndex());
        }
    }

    private String renderNode(Node node) {
        Net net = node.getNet();
        String nodeName = getNodeId(node);
        sb.append(" ").append(nodeName);
        int value = node.getValue(Permissions.DIRECT);
        if (value == 0) {
            if (isTarget(node)) {
                TableBuilder table = new TableBuilder();
                table.textCenter = "";
                table.fillColor = COLOR_DOT;
                table.rounded = true;
                table.vanishEmptySides = true;
                table.cellpaddingTable = 0;
                table.cellpaddingCenter = 5;
                table.outlineColors = getOutlineColors(node);
                table.renderTable(sb);
            } else {
                sb.append("[shape=Mrecord label=\"<l>|<r>\" width=0.15 height=0.15 fixedsize=true color=%s]"
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
                        sb.append(" color=\"%s\"".formatted(color));
                    }
                    sb.append("]");
                }
            } else {
                Split split = Splits.getByValue(value);
                if (split != null) {
                    if (isTarget(node)) {
                        TableBuilder table = new TableBuilder();
                        table.fillColor = COLOR_SPLIT;
                        table.textCenter = escape(split.getShortName());
                        table.rounded = false;
                        table.outlineColors = getOutlineColors(node);
                        table.renderTable(sb);
                    } else {
                        sb.append("[shape=record][label=\"<l>|%s|<r>\"][color=%s]"
                                .formatted(escape(split.getShortName()), COLOR_SPLIT));
                    }
                } else {
                    String hexString = Integer.toHexString(value);
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
                        if (isTarget(node)) {
                            List<String> outlineColors = getOutlineColors(node);
                            TableBuilder table = new TableBuilder();
                            table.textCenter = hexString;
                            table.fillColor = COLOR_NODE;
                            table.rounded = false;
                            table.outlineColors = outlineColors;
                            table.renderTable(sb);
                        } else {
                            sb.append("[shape=record][label=\"<l>|").append(hexString).append("|<r>\"]");
                        }
                    }
                }
            }
        }
        String xlabel = "";
        if (node == net.getRoot()) {
            xlabel = "⤷";
        }
        String name = net.getName(node);
        if (name != null) {
            xlabel += name;
        }
        String annotation = net.getAnnotation(node);
        if (annotation != null) {
            xlabel += annotation;
        }
        if (!xlabel.isEmpty()) {
            sb.append("[xlabel=\"%s\"]".formatted(escape(xlabel)));
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
            sb.append(">%s</td>".formatted(escapeHtml(textCenter)));
        }

        private static String escapeHtml(String text) {
            return StringEscapeUtils.escapeHtml4(text);
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
        if (command.getValue() == Commands.CONCLUSION_SUCCESS.getValue()) {
            return "#daffb5";
        } else if (command.getValue() == Commands.CONCLUSION_FAILURE.getValue()) {
            return "#ffcfb5";
        }
        return COLOR_NODE;
    }

    private boolean isTarget(Node node) {
        return node == target1 || node == target2 || node == target3;
    }

    private static void collectNodesRightToLeft(Node node, Set<Node> collected) {
        if (!collected.add(node)) {
            return;
        }
        collectNodesRightToLeft(node.getRightChild(Permissions.DIRECT), collected);
        collectNodesRightToLeft(node.getLeftChild(Permissions.DIRECT), collected);
    }

    private static String escape(String shortName) {
        return shortName.replaceAll("<", "&lt;")
                .replaceAll("[\\\\]", "\\\\\\\\")
                .replaceAll("[|]", "\\\\|");
    }
}
