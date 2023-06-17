package org.ent.dev.plan;

import org.ent.NetEventListener;
import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.ExecutionResult;
import org.ent.run.EntRunnerListener;

import java.util.HashMap;
import java.util.Map;

public class VariabilityCollector implements NetEventListener, EntRunnerListener {

    final Map<Command, CommandData> commandDataMap = new HashMap<>();
    final Map<Arrow, ArrowData> arrowDataMap = new HashMap<>();
    final NewNodeData newNodeData = new NewNodeData();

    static class CommandData {
        private final Command command;
        private int timesExecuted;

        public CommandData(Command command) {
            this.command = command;
        }

        public int getTimesExecuted() {
            return timesExecuted;
        }

        public void executed() {
            timesExecuted++;
        }
    }

    static class ArrowData {
        private final Arrow arrow;
        private int timesRead;
        private int timesWritten;

        public ArrowData(Arrow arrow) {
            this.arrow = arrow;
        }

        public void wasRead() {
            timesRead++;
        }

        public void wasWritten() {
            timesWritten++;
        }

        public Arrow getArrow() {
            return arrow;
        }

        public int getTimesRead() {
            return timesRead;
        }

        public int getTimesWritten() {
            return timesWritten;
        }
    }

    static class NewNodeData {
        private int numCNode;
        private int numUNode;
        private int numBNode;

        public void newCNode() {
            numCNode++;
        }

        public void newUNode() {
            numUNode++;
        }

        public void newBNode() {
            numBNode++;
        }

        public int getNumCNode() {
            return numCNode;
        }

        public int getNumUNode() {
            return numUNode;
        }

        public int getNumBNode() {
            return numBNode;
        }
    }

    @Override
    public void calledGetChild(Node node, ArrowDirection arrowDirection, Purview purview) {
        if (purview == Purview.COMMAND) {
            ArrowData arrowData = getArrowData(node.getArrow(arrowDirection));
            arrowData.wasRead();
        }
    }

    @Override
    public void calledSetChild(Node from, ArrowDirection arrowDirection, Node to, Purview purview) {
        if (purview == Purview.COMMAND) {
            ArrowData arrowData = getArrowData(from.getArrow(arrowDirection));
            arrowData.wasWritten();
        }
    }

    @Override
    public void calledNewNode(Node n) {
        switch (n.getNodeType()) {
            case COMMAND_NODE -> newNodeData.newCNode();
            case UNARY_NODE -> newNodeData.newUNode();
            case BINARY_NODE -> newNodeData.newBNode();
            case MARKER_NODE -> {}
        }
    }

    @Override
    public void getValue(Node node, Purview purview) {

    }

    @Override
    public void setValue(Node node, int previousValue, int newValue) {

    }

    @Override
    public void evaluatedIsIdenticalCondition(Node node1, Node node2) {

    }

    @Override
    public void fireCommandExecuted(Node commandNode, ExecutionResult executeResult) {
        CommandData commandData = getCommandData(commandNode.getCommand());
        commandData.executed();
    }

    private CommandData getCommandData(Command command) {
        return commandDataMap.computeIfAbsent(command, key -> new CommandData(command));
    }

    private ArrowData getArrowData(Arrow arrow) {
        return arrowDataMap.computeIfAbsent(arrow, key -> new ArrowData(arrow));
    }

}
