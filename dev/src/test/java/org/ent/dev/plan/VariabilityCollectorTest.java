package org.ent.dev.plan;

import org.ent.dev.DefaultTestRunSetup;
import org.ent.dev.plan.VariabilityCollector.ArrowData;
import org.ent.dev.unit.data.DataImpl;
import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.DefaultNetController;
import org.ent.net.ExecutionContext;
import org.ent.net.Net;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.io.parser.NetParser;
import org.ent.net.io.parser.ParserException;
import org.ent.net.node.BNode;
import org.ent.net.node.CNode;
import org.ent.net.node.MarkerNode;
import org.ent.net.node.UNode;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.CommandFactory;
import org.ent.net.node.cmd.ExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VariabilityCollectorTest {

    private static final CNode cNodeNop = new CNode(CommandFactory.NOP_COMMAND);
    private static final CNode cNodeIx = new CNode(CommandFactory.ANCESTOR_SWAP_COMMAND);

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

        private CNode cNodeNop;
        private UNode uNode;

        @BeforeEach
        void setUp() {
            Net net = new Net();
            DefaultNetController controller = new DefaultNetController(net);
            cNodeNop = controller.newCNode(CommandFactory.NOP_COMMAND);
            uNode = controller.newUNode();
        }

        @Test
        void fireSetChild() {
            VariabilityCollector collector = new VariabilityCollector();

            collector.fireSetChild(uNode, ArrowDirection.DOWN, cNodeNop, ExecutionContext.COMMAND);
            collector.fireSetChild(uNode, ArrowDirection.DOWN, cNodeNop, ExecutionContext.COMMAND);

            Map<Arrow, ArrowData> arrowDataMap = collector.arrowDataMap;
            assertThat(arrowDataMap).containsOnlyKeys(uNode.getArrow());
            assertThat(arrowDataMap.get(uNode.getArrow()).getTimesRead()).isZero();
            assertThat(arrowDataMap.get(uNode.getArrow()).getTimesWritten()).isEqualTo(2);
        }

        @Test
        void fireGetChild() {
            VariabilityCollector collector = new VariabilityCollector();

            collector.fireGetChild(uNode, ArrowDirection.DOWN, ExecutionContext.COMMAND);

            Map<Arrow, ArrowData> arrowDataMap = collector.arrowDataMap;
            assertThat(arrowDataMap).containsOnlyKeys(uNode.getArrow());
            assertThat(arrowDataMap.get(uNode.getArrow()).getTimesRead()).isEqualTo(1);
            assertThat(arrowDataMap.get(uNode.getArrow()).getTimesWritten()).isZero();
        }

    }

    @Nested
    class Integration {

        @Test
        void arrows() throws ParserException {
            VariabilityExam exam = new VariabilityExam(DefaultTestRunSetup.RUN_SETUP);

            NetParser parser = new NetParser()
                    .permitMarkerNodes(new MarkerNode());
            Net net = parser.parse("outer=(command1=(<|:*>, arguments=(toSet=[#], <nop>)), toSet)");
            BNode nodeOuter = (BNode) parser.getNodeNames().get("outer");
            BNode nodeCommand1 = (BNode) parser.getNodeNames().get("command1");
            BNode nodeArguments = (BNode) parser.getNodeNames().get("arguments");
            UNode nodeToSet = (UNode) parser.getNodeNames().get("toSet");


            VariabilityExamData data = new VariabilityExamData(new DataImpl());
            data.setReplicator(() -> net);

            exam.accept(data);

            net.referentialGarbageCollection();
            String netFmt = new NetFormatter().withAscii(true).format(net);
            assertThat(netFmt).isEqualTo("[<nop>]");

            VariabilityCollector collector = exam.getCollector();
            assertThat(collector.arrowDataMap).containsOnlyKeys(
                    nodeToSet.getArrow(),
                    nodeOuter.getLeftArrow(),
                    nodeOuter.getRightArrow(),
                    nodeCommand1.getLeftArrow(),
                    nodeCommand1.getRightArrow(),
                    nodeArguments.getLeftArrow(),
                    nodeArguments.getRightArrow()
            );
            ArrowData arrowToSet = collector.arrowDataMap.get(nodeToSet.getArrow());
            assertThat(arrowToSet.getTimesRead()).isZero();
            assertThat(arrowToSet.getTimesWritten()).isEqualTo(1);
            ArrowData arrowOuterLeft = collector.arrowDataMap.get(nodeOuter.getLeftArrow());
            assertThat(arrowOuterLeft.getTimesRead()).isEqualTo(1);
            assertThat(arrowOuterLeft.getTimesWritten()).isZero();
            ArrowData arrowOuterRight = collector.arrowDataMap.get(nodeOuter.getRightArrow());
            assertThat(arrowOuterRight.getTimesRead()).isEqualTo(1);
            assertThat(arrowOuterRight.getTimesWritten()).isZero();
        }

    }
}