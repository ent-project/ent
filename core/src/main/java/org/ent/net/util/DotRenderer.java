package org.ent.net.util;

import org.ent.net.Arrow;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.veto.Veto;
import org.ent.net.node.cmd.veto.Vetos;

public class DotRenderer {
    public static String render(Net net) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph G {\n");
        sb.append(" node[style=filled]\n");
        sb.append(" edge[arrowsize=0.6]\n");
        for (Node node : net.getNodes()) {
            sb.append(" n").append(node.getIndex());
            int value = node.getValue(Purview.DIRECT);
            if (value == 0) {
                sb.append("[shape=Mrecord][label=\"<l>|<r>\", width=0.1, height=0.1, fixedsize=true, style=filled, fillcolor=black]\n");
            } else {
                Command command = Commands.getByValue(value);
                if (command != null) {
                    sb.append("[shape=Mrecord][label=\"<l>|").append(escape(command.getShortName())).append("|<r>\"]\n");
                } else {
                    Veto veto = Vetos.getByValue(value);
                    if (veto != null) {
                        sb.append("[shape=record][label=\"<l>|").append(escape(veto.getShortName())).append("|<r>\"]\n");
                    } else {
                        sb.append("[shape=record][label=\"<l>|").append(Integer.toHexString(value)).append("|<r>\"]\n");
                    }
                }
            }
            for (Arrow arrow : node.getArrows()) {
                Node child = arrow.getTarget(Purview.DIRECT);
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
                sb.append("\n");
            }
        }
        sb.append("}\n");
        return sb.toString();
    }

    private static String escape(String shortName) {
        return shortName.replaceAll("<", "&lt;")
                .replaceAll("[\\\\]", "\\\\\\\\");
    }
}
