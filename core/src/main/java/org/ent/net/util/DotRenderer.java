package org.ent.net.util;

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

        Set<Node> nodesConnectedToRoot = new LinkedHashSet<>();
        collectNodesRightToLeft(net.getRoot(), nodesConnectedToRoot);

        StringBuilder sb = new StringBuilder();
        sb.append("digraph G {\n");
        sb.append(" node[style=filled];\n");
        sb.append(" edge[arrowsize=0.6];\n");

        for (Node node : nodesConnectedToRoot) {
            sb.append(" n").append(node.getIndex());
            int value = node.getValue(Purview.DIRECT);
            if (value == 0) {
                sb.append("[shape=Mrecord][label=\"<l>|<r>\", width=0.1, height=0.1, fixedsize=true, style=filled, fillcolor=black]");
            } else {
                Command command = Commands.getByValue(value);
                if (command != null) {
                    sb.append("[shape=Mrecord][label=\"<l>|").append(escape(command.getShortName())).append("|<r>\"]");
                } else {
                    Veto veto = Vetos.getByValue(value);
                    if (veto != null) {
                        sb.append("[shape=record][label=\"<l>|").append(escape(veto.getShortName())).append("|<r>\"]");
                    } else {
                        sb.append("[shape=record][label=\"<l>|").append(Integer.toHexString(value)).append("|<r>\"]");
                    }
                }
            }
            if (node == net.getRoot()) {
                sb.append("[fillcolor=wheat2]");
            }
            sb.append(";\n");
            for (Arrow arrow : node.getArrows()) {
                Node child = arrow.getTarget(Purview.DIRECT);
                if (node == child) {
                    continue;
                }
                sb.append(" \"n").append(node.getIndex()).append("\":");
                sb.append(switch (arrow.getDirection()) {
                    case LEFT -> "l";
                    case RIGHT -> "r";
                });
                sb.append(" -> n").append(child.getIndex());
                sb.append(switch (arrow.getDirection()) {
                    case LEFT -> " [color=purple]";
                    case RIGHT -> " [color=green]";
                });
                sb.append(";\n");
            }
        }
        sb.append("}\n");
        return sb.toString();
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
