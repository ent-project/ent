package org.ent.dev.game.forwardarithmetic;

import org.ent.Ent;
import org.ent.EntHash;
import org.ent.NopEntEventListener;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;

import java.util.HashSet;
import java.util.Set;

public class HashEntEventListener extends NopEntEventListener {
    Set<Integer> hashes = new HashSet<>();
    Integer firstRepetition;
    private final Ent ent;

    HashEntEventListener(Ent ent) {
        this.ent = ent;
    }

    @Override
    public void beforeCommandExecution(Node executionPointer, Command command) {
        if (firstRepetition == null) {
            int hash = EntHash.hash(ent);
            boolean isNew = hashes.add(hash);
            if (!isNew) {
                firstRepetition = hashes.size();
            }
        }
    }
}
