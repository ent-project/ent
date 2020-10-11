package org.ent.dev.plan;

import org.ent.dev.DefaultTestRunSetup;
import org.ent.dev.NetReplicator;
import org.ent.net.Net;
import org.ent.net.io.parser.NetParser;
import org.ent.net.io.parser.ParserException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class VariabilityExamTest {

    @Test
    @Disabled
    void doAccept() throws ParserException {
        VariabilityExam exam = new VariabilityExam(DefaultTestRunSetup.RUN_SETUP);
        NetReplicator replicator = Mockito.mock(NetReplicator.class);

        NetParser parser = new NetParser();
        Net net = parser.parse("");

        Mockito.when(replicator.getNewSpecimen()).thenReturn(net);
        VariabilityExamData data = new VariabilityExamData();
        data.setReplicator(replicator);

        exam.doAccept(data);
    }
}