package org.ent.dev.plan;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.ent.dev.DefaultTestRunSetup;
import org.ent.dev.plan.VariabilityCollector.ArrowData;
import org.ent.dev.unit.data.DataImpl;
import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.io.parser.NetParser;
import org.ent.net.io.parser.ParserException;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.ExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SoftAssertionsExtension.class)
class VariabilityCollectorTest {

    private Net net;
    private Node cNodeNop;
    private Node cNodeX;
    private Node uNode;

    @BeforeEach
    void setUp() {
        net = new Net();
        cNodeNop = net.newCNode(Commands.NOP);
        cNodeX = net.newCNode(Commands.ANCESTOR_EXCHANGE);
        uNode = net.newUNode(cNodeNop);
    }

    @Test
    void fireCommandExecuted() {
        VariabilityCollector collector = new VariabilityCollector();

        for (Node cNode : List.of(cNodeNop, cNodeX, cNodeNop)) {
            collector.fireCommandExecuted(cNode, ExecutionResult.NORMAL);
        }

        Map<Command, VariabilityCollector.CommandData> commandDataMap = collector.commandDataMap;
        assertThat(commandDataMap.get(cNodeNop.getCommand()).getTimesExecuted()).isEqualTo(2);
        assertThat(commandDataMap.get(cNodeX.getCommand()).getTimesExecuted()).isEqualTo(1);
    }

    @Nested
    class Arrows {

        @Test
        void fireSetChild() {
            VariabilityCollector collector = new VariabilityCollector();

            collector.calledSetChild(uNode, ArrowDirection.LEFT, cNodeNop, Purview.COMMAND);
            collector.calledSetChild(uNode, ArrowDirection.LEFT, cNodeNop, Purview.COMMAND);

            Map<Arrow, ArrowData> arrowDataMap = collector.arrowDataMap;
            assertThat(arrowDataMap).containsOnlyKeys(uNode.getLeftArrow());
            assertThat(arrowDataMap.get(uNode.getLeftArrow()).getTimesRead()).isZero();
            assertThat(arrowDataMap.get(uNode.getLeftArrow()).getTimesWritten()).isEqualTo(2);
        }

        @Test
        void fireGetChild() {
            VariabilityCollector collector = new VariabilityCollector();

            collector.calledGetChild(uNode, ArrowDirection.LEFT, Purview.COMMAND);

            Map<Arrow, ArrowData> arrowDataMap = collector.arrowDataMap;
            assertThat(arrowDataMap).containsOnlyKeys(uNode.getLeftArrow());
            assertThat(arrowDataMap.get(uNode.getLeftArrow()).getTimesRead()).isEqualTo(1);
            assertThat(arrowDataMap.get(uNode.getLeftArrow()).getTimesWritten()).isZero();
        }
    }

    @Nested
    class NewNode {

        @Test
        void newCNode(SoftAssertions softly) {
            VariabilityCollector collector = new VariabilityCollector();

            collector.calledNewNode(cNodeX);

            softly.assertThat(collector.newNodeData.getNumCNode()).isEqualTo(1);
            softly.assertThat(collector.newNodeData.getNumUNode()).isZero();
            softly.assertThat(collector.newNodeData.getNumBNode()).isZero();
        }

        @Test
        void newUNode(SoftAssertions softly) {
            VariabilityCollector collector = new VariabilityCollector();
            Node nop = net.newCNode(Commands.NOP);

            collector.calledNewNode(uNode);

            softly.assertThat(collector.newNodeData.getNumCNode()).isZero();
            softly.assertThat(collector.newNodeData.getNumUNode()).isEqualTo(1);
            softly.assertThat(collector.newNodeData.getNumBNode()).isZero();
        }

        @Test
        void newBNode(SoftAssertions softly) {
            VariabilityCollector collector = new VariabilityCollector();
            Node nop = net.newCNode(Commands.NOP);
            Node bNode = net.newNode(nop, nop);

            collector.calledNewNode(bNode);

            softly.assertThat(collector.newNodeData.getNumCNode()).isZero();
            softly.assertThat(collector.newNodeData.getNumUNode()).isZero();
            softly.assertThat(collector.newNodeData.getNumBNode()).isEqualTo(1);
        }
    }

    @Nested
    class Integration {

        private VariabilityExam exam;
        private NetParser parser;
        private VariabilityExamData data;

        @BeforeEach
        void setUp() {
            exam = new VariabilityExam(DefaultTestRunSetup.RUN_SETUP);
            parser = new NetParser().permitMarkerNodes();
            data = new VariabilityExamData(new DataImpl());
        }

        @Test
        void arrows(SoftAssertions softly) throws ParserException {
            Net net = parser.parse("</=>(arguments:(toSet:[@], <o>), toSet)");
            data.setReplicator(() -> net);
            Node nodeArguments = parser.getNodeByName("arguments");
            Node nodeToSet = parser.getNodeNames().get("toSet");

            exam.accept(data);

            net.referentialGarbageCollection();
            String netFmt = new NetFormatter().withAscii(true).format(net);
            softly.assertThat(netFmt).isEqualTo("[<o>]");

            VariabilityCollector collector = exam.getCollector();
            softly.assertThat(collector.arrowDataMap).containsOnlyKeys(
                    nodeToSet.getLeftArrow(),
                    nodeArguments.getLeftArrow(),
                    nodeArguments.getRightArrow()
            );
            ArrowData arrowToSet = collector.arrowDataMap.get(nodeToSet.getLeftArrow());
            softly.assertThat(arrowToSet.getTimesRead()).isZero();
            softly.assertThat(arrowToSet.getTimesWritten()).isEqualTo(1);
            ArrowData arrowArgumentsLeft = collector.arrowDataMap.get(nodeArguments.getLeftArrow());
            softly.assertThat(arrowArgumentsLeft.getTimesRead()).isEqualTo(1);
            softly.assertThat(arrowArgumentsLeft.getTimesWritten()).isZero();
            ArrowData arrowArgumentsRight = collector.arrowDataMap.get(nodeArguments.getRightArrow());
            softly.assertThat(arrowArgumentsRight.getTimesRead()).isEqualTo(1);
            softly.assertThat(arrowArgumentsRight.getTimesWritten()).isZero();
        }

        @Test
        void newNode(SoftAssertions softly) throws ParserException {
            Net net = parser.parse("</dupn>(arguments:(toSet:[@], <o>), toSet)");
            data.setReplicator(() -> net);

            exam.accept(data);

            net.referentialGarbageCollection();
            String netFmt = new NetFormatter().withAscii(true).format(net);
            softly.assertThat(netFmt).isEqualTo("[<o>]");
            VariabilityCollector collector = exam.getCollector();
            softly.assertThat(collector.newNodeData.getNumCNode()).isEqualTo(1);
            softly.assertThat(collector.newNodeData.getNumUNode()).isEqualTo(0);
            softly.assertThat(collector.newNodeData.getNumBNode()).isEqualTo(0);
        }

    }
}