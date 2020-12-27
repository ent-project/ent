package org.ent.dev.plan;

import org.ent.ExecutionEventListener;
import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.ExecutionContext;
import org.ent.net.node.CNode;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.ExecutionResult;
import org.ent.run.NetRunnerListener;

import java.util.HashMap;
import java.util.Map;

public class VariabilityCollector implements ExecutionEventListener, NetRunnerListener {

    Map<Command, CommandData> commandDataMap = new HashMap<>();
    Map<Arrow, ArrowData> arrowDataMap = new HashMap<>();

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

    @Override
    public void fireExecutionStart() {

    }

    @Override
    public void fireGetChild(Node node, ArrowDirection arrowDirection, ExecutionContext context) {
        ArrowData arrowData = getArrowData(node.getArrow(arrowDirection));
        arrowData.wasRead();
    }

    @Override
    public void fireSetChild(Node from, ArrowDirection arrowDirection, Node to, ExecutionContext context) {
        ArrowData arrowData = getArrowData(from.getArrow(arrowDirection));
        arrowData.wasWritten();
    }

    @Override
    public void fireNewNode(Node n) {

    }

    @Override
    public void fireCommandExecuted(CNode commandNode, ExecutionResult executeResult) {
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
