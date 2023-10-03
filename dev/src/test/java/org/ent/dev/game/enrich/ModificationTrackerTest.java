package org.ent.dev.game.enrich;

import org.ent.Ent;
import org.ent.Profile;
import org.ent.net.Net;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.accessor.Accessors;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.run.EntRunner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ent.util.NetBuilder.*;

class ModificationTrackerTest {

    @BeforeAll
    static void setTestEnvironment() {
        Profile.setTest(true);
    }

    @Test
    void noModification() {
        Net domain = builder().net(node(value(1), value(2)));
        Ent ent = builder().ent(
                node(Commands.get(Operations.SET_OPERATION, Accessors.R, Accessors.LL), domain.getRoot(), ignored()));
        ent.addDomain(domain);

        ModificationTracker modificationTracker = new ModificationTracker();
        domain.addEventListener(modificationTracker);
        EntRunner entRunner = new EntRunner(ent);

        entRunner.step();

        assertThat(modificationTracker.wasModified()).isFalse();
    }

    public static Stream<Command> modifyingCommands() {
        return Stream.of(
                Commands.SET,
                Commands.SET_VALUE,
                Commands.get(Operations.INC_OPERATION));
    }

    @ParameterizedTest
    @MethodSource("modifyingCommands")
    void modify(Command command) {
        Net domain = builder().net(node(value(1), value(2)));
        Ent ent = builder().ent(node(command, domain.getRoot(), ignored()));

        ModificationTracker modificationTracker = new ModificationTracker();
        domain.addEventListener(modificationTracker);
        EntRunner entRunner = new EntRunner(ent);

        entRunner.step();

        assertThat(modificationTracker.wasModified()).isTrue();
    }

    @Test
    void ancestorExchange() {
        Net domain = builder().net(node(value(1), value(2)));
        Ent ent = builder().ent(node(Commands.ANCESTOR_EXCHANGE, domain.getRoot(), ignored()));

        ModificationTracker modificationTracker = new ModificationTracker();
        domain.addEventListener(modificationTracker);
        EntRunner entRunner = new EntRunner(ent);

        entRunner.step();

        assertThat(modificationTracker.wasModified()).isTrue();
    }

}