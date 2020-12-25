package org.ent.dev.plan;

import com.google.common.math.IntMath;
import org.ent.ExecutionEventListener;
import org.ent.net.ArrowDirection;
import org.ent.net.node.CNode;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.ExecutionResult;
import org.ent.run.NetRunnerListener;

import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class VariabilityEvaluator implements ExecutionEventListener, NetRunnerListener {

    public static final int MAX_LEVEL_YIELDING_POINTS = 12;
    public static final int BASE_POINT_VALUE = 1000;
    private static final long[] CUMULATIVE_POINTS_DECAYING = new long[MAX_LEVEL_YIELDING_POINTS + 1];
    static {
        for (int i = 0; i < CUMULATIVE_POINTS_DECAYING.length; i++) {
            CUMULATIVE_POINTS_DECAYING[i] = calculateCumulativePointsDecaying(i);
        }
    }

    Map<Command, CommandData> commandDataMap = new HashMap<>();

    private static class CommandData {
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

    public long getPoints() {
        return commandDataMap.entrySet().stream()
                .map(e -> e.getValue().getTimesExecuted())
                .map(level -> getCumulativePointsDecaying(level))
                .mapToLong(Long::longValue)
                .sum();
    }

    static long getDecayingPoints(int level) {
        if (level < 1) {
            throw new IllegalArgumentException("Argument must be >= 1, but was " + level);
        }
        if (level <= MAX_LEVEL_YIELDING_POINTS) {
            return IntMath.divide(BASE_POINT_VALUE, IntMath.pow(2, level - 1), RoundingMode.HALF_UP);
        } else {
            return 0;
        }
    }

    public static long getCumulativePointsDecaying(int level) {
        if (level > MAX_LEVEL_YIELDING_POINTS) {
            return CUMULATIVE_POINTS_DECAYING[CUMULATIVE_POINTS_DECAYING.length - 1];
        } else {
            return CUMULATIVE_POINTS_DECAYING[level];
        }
    }

    private static long calculateCumulativePointsDecaying(int level) {
        if (level < 0) {
            throw new IllegalArgumentException("Argument must be >= 0, but was " + level);
        }
        long sum = 0L;
        for (int i = Math.min(level, MAX_LEVEL_YIELDING_POINTS); i >= 1; i--) {
            sum += getDecayingPoints(i);
        }
        return sum;
    }

}
