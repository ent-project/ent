package org.ent.dev.plan;

import org.ent.dev.DefaultTestRunSetup;
import org.ent.dev.unit.data.DataImpl;
import org.ent.net.Net;
import org.ent.net.io.parser.NetParser;
import org.ent.net.io.parser.ParserException;
import org.ent.net.node.MarkerNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VariabilityExamTest {

    @Test
    void doAccept() throws ParserException {
        VariabilityExam exam = new VariabilityExam(DefaultTestRunSetup.RUN_SETUP);

        NetParser parser = new NetParser();
        parser.permitMarkerNodes(new MarkerNode());
        Net net = parser.parse("((<nop>, (#,#)), #)");

        VariabilityExamData data = new VariabilityExamData(new DataImpl());
        data.setReplicator(() -> net);

        exam.accept(data);

        VariabilityExamResult result = data.getVariabilityExamResult();
        assertThat(result.getPoints()).isEqualTo(1800L);
    }

}