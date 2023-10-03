package org.ent.dev.game.enrich;

import org.ent.listener.NopEntEventListener;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;

import java.util.HashSet;
import java.util.Set;

class AntEntFitnessTracker extends NopEntEventListener {

    public Set<Command> commandsExecuted = new HashSet<>();

    @Override
    public void beforeCommandExecution(Node executionPointer, Command command) {
        if (command != null) {
            commandsExecuted.add(command);
        }
    }
}
