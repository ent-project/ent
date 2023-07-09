package org.ent.util;

import org.ent.Ent;
import org.ent.net.Arrow;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.veto.Veto;
import org.ent.net.node.cmd.veto.Vetos;

import java.util.LinkedHashSet;
import java.util.Set;

public class DotRenderer {
    public static String render(Net net) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph G {\n");
        sb.append(" node[style=filled];\n");
        sb.append(" edge[arrowsize=0.6];\n");
        renderNetNodes(net, sb, "n");
        sb.append("}\n");
        return sb.toString();
    }

    public static String render(Ent ent) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph G {\n");
        sb.append(" node[style=filled, color=ivory2];\n");
        sb.append(" edge[arrowsize=0.6];\n");
        renderNetNodes(ent.getNet(), sb, "n");
        for (int i = 0; i < ent.getDomains().size(); i++) {
            Net domain = ent.getDomains().get(i);
            sb.append(" subgraph cluster_").append(i).append(" {\n");
            sb.append(" color=\"gray\";\n");
            if (domain.getName() != null) {
                sb.append(" label=\"").append(domain.getName()).append("\";\n");
                sb.append(" fontcolor=\"gray\";\n");
                sb.append(" labeljust=\"l\";\n");
            }
            renderNetNodes(domain, sb, "d"+i+"n");
            sb.append(" }\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

    private static void renderNetNodes(Net net, StringBuilder sb, String nodePrefix) {
        Set<Node> nodesConnectedToRoot = new LinkedHashSet<>();
        collectNodesRightToLeft(net.getRoot(), nodesConnectedToRoot);
        for (Node node : nodesConnectedToRoot) {
            String nodeName = nodePrefix + node.getIndex();
            sb.append(" ").append(nodeName);
            int value = node.getValue(Purview.DIRECT);
            if (value == 0) {
                sb.append("[shape=Mrecord][label=\"<l>|<r>\", width=0.1, height=0.1, fixedsize=true, style=filled, color=black]");
            } else {
                Command command = Commands.getByValue(value);
                if (command != null) {
                    sb.append("[shape=Mrecord][label=\"<l>|").append(escape(command.getShortName())).append("|<r>\"]");
                    if (command.getValue() == Commands.FINAL_SUCCESS.getValue()) {
                        sb.append("[color=palegreen]");
                    } else if (command.getValue() == Commands.FINAL_FAILURE.getValue()) {
                        sb.append("[color=mistyrose2]");
                    }
                } else {
                    Veto veto = Vetos.getByValue(value);
                    if (veto != null) {
                        sb.append("[shape=record][label=\"<l>|").append(escape(veto.getShortName())).append("|<r>\"][color=beige]");
                    } else {
                        String hexString = Integer.toHexString(value);
                        if (node.isLeafNode()) {
                            sb.append("[shape=circle, label=\"").append(hexString).append("\"]");
                        } else {
                            sb.append("[shape=record][label=\"<l>|").append(hexString).append("|<r>\"]");
                        }
                    }
                }
            }
            if (node == net.getRoot()) {
                sb.append("[color=slategray1]");
            }
            sb.append(";\n");
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
                sb.append(";\n");
            }
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
