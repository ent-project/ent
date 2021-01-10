package org.ent.dev.plan;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.ent.dev.DefaultTestRunSetup;
import org.ent.dev.plan.VariabilityCollector.ArrowData;
import org.ent.dev.unit.data.DataImpl;
import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Manner;
import org.ent.net.Net;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.io.parser.NetParser;
import org.ent.net.io.parser.ParserException;
import org.ent.net.node.BNode;
import org.ent.net.node.CNode;
import org.ent.net.node.UNode;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.CommandFactory;
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
    private CNode cNodeNop;
    private CNode cNodeIx;
    private UNode uNode;

    @BeforeEach
    void setUp() {
        net = new Net();
        cNodeNop = net.newCNode(CommandFactory.NOP_COMMAND);
        cNodeIx = net.newCNode(CommandFactory.ANCESTOR_SWAP_COMMAND);
        uNode = net.newUNode();
    }

    @Test
    void fireCommandExecuted() {
        VariabilityCollector collector = new VariabilityCollector();

        for (CNode cNode : List.of(cNodeNop, cNodeIx, cNodeNop)) {
            collector.fireCommandExecuted(cNode, ExecutionResult.NORMAL);
        }

        Map<Command, VariabilityCollector.CommandData> commandDataMap = collector.commandDataMap;
        assertThat(commandDataMap.get(cNodeNop.getCommand()).getTimesExecuted()).isEqualTo(2);
        assertThat(commandDataMap.get(cNodeIx.getCommand()).getTimesExecuted()).isEqualTo(1);
    }

    @Nested
    class Arrows {

        @Test
        void fireSetChild() {
            VariabilityCollector collector = new VariabilityCollector();

            collector.calledSetChild(uNode, ArrowDirection.DOWN, cNodeNop, Manner.COMMAND);
            collector.calledSetChild(uNode, ArrowDirection.DOWN, cNodeNop, Manner.COMMAND);

            Map<Arrow, ArrowData> arrowDataMap = collector.arrowDataMap;
            assertThat(arrowDataMap).containsOnlyKeys(uNode.getArrow());
            assertThat(arrowDataMap.get(uNode.getArrow()).getTimesRead()).isZero();
            assertThat(arrowDataMap.get(uNode.getArrow()).getTimesWritten()).isEqualTo(2);
        }

        @Test
        void fireGetChild() {
            VariabilityCollector collector = new VariabilityCollector();

            collector.calledGetChild(uNode, ArrowDirection.DOWN, Manner.COMMAND);

            Map<Arrow, ArrowData> arrowDataMap = collector.arrowDataMap;
            assertThat(arrowDataMap).containsOnlyKeys(uNode.getArrow());
            assertThat(arrowDataMap.get(uNode.getArrow()).getTimesRead()).isEqualTo(1);
            assertThat(arrowDataMap.get(uNode.getArrow()).getTimesWritten()).isZero();
        }
    }

    @Nested
    class NewNode {

        @Test
        void newCNode(SoftAssertions softly) {
            VariabilityCollector collector = new VariabilityCollector();

            collector.calledNewNode(cNodeIx);

            softly.assertThat(collector.newNodeData.getNumCNode()).isEqualTo(1);
            softly.assertThat(collector.newNodeData.getNumUNode()).isZero();
            softly.assertThat(collector.newNodeData.getNumBNode()).isZero();
        }

        @Test
        void newUNode(SoftAssertions softly) {
            VariabilityCollector collector = new VariabilityCollector();

            collector.calledNewNode(uNode);

            softly.assertThat(collector.newNodeData.getNumCNode()).isZero();
            softly.assertThat(collector.newNodeData.getNumUNode()).isEqualTo(1);
            softly.assertThat(collector.newNodeData.getNumBNode()).isZero();
        }

        @Test
        void newBNode(SoftAssertions softly) {
            VariabilityCollector collector = new VariabilityCollector();
            BNode bNode = net.newBNode();

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
            Net net = parser.parse("((<|:*>, arguments=(toSet=[#], <nop>)), toSet)");
            data.setReplicator(() -> net);
            BNode nodeArguments = (BNode) parser.getNodeNames().get("arguments");
            UNode nodeToSet = (UNode) parser.getNodeNames().get("toSet");

            exam.accept(data);

            net.referentialGarbageCollection();
            String netFmt = new NetFormatter().withAscii(true).format(net);
            softly.assertThat(netFmt).isEqualTo("[<nop>]");

            VariabilityCollector collector = exam.getCollector();
            softly.assertThat(collector.arrowDataMap).containsOnlyKeys(
                    nodeToSet.getArrow(),
                    nodeArguments.getLeftArrow(),
                    nodeArguments.getRightArrow()
            );
            ArrowData arrowToSet = collector.arrowDataMap.get(nodeToSet.getArrow());
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
            Net net = parser.parse("((<|dup*>, arguments=(toSet=[#], <nop>)), toSet)");
            data.setReplicator(() -> net);

            exam.accept(data);

            net.referentialGarbageCollection();
            String netFmt = new NetFormatter().withAscii(true).format(net);
            softly.assertThat(netFmt).isEqualTo("[<nop>]");
            VariabilityCollector collector = exam.getCollector();
            softly.assertThat(collector.newNodeData.getNumCNode()).isEqualTo(1);
            softly.assertThat(collector.newNodeData.getNumUNode()).isEqualTo(0);
            softly.assertThat(collector.newNodeData.getNumBNode()).isEqualTo(0);
        }

    }
}