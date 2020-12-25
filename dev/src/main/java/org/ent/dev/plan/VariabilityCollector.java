package org.ent.dev.plan;

import org.ent.ExecutionEventListener;
import org.ent.net.ArrowDirection;
import org.ent.net.node.CNode;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.ExecutionResult;
import org.ent.run.NetRunnerListener;

import java.util.HashMap;
import java.util.Map;

public class VariabilityCollector implements ExecutionEventListener, NetRunnerListener {

    Map<Command, CommandData> commandDataMap = new HashMap<>();

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

    @Override
    public void fireExecutionStart() {

    }

    @Override
    public void fireGetChild(Node n, ArrowDirection arrowDirection) {

    }

    @Override
    public void fireSetChild(Node from, ArrowDirection arrowDirection, Node to) {

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
        return commandDataMap.computeIfAbsent(command, $ -> new CommandData(command));
    }

}
