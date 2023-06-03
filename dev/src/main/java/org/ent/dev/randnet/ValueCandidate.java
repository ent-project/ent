package org.ent.dev.randnet;

import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.veto.Veto;

public record ValueCandidate(int value, int weight) {

    public ValueCandidate(Command command, int weight) {
        this(command.getValue(), weight);
    }

    public ValueCandidate(Veto veto, int weight) {
        this(veto.getValue(), weight);
    }
}