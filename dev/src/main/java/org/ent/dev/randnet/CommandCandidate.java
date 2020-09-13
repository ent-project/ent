package org.ent.dev.randnet;

import org.ent.net.node.cmd.Command;

public record CommandCandidate(Command command, double weight) {
}