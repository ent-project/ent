package org.ent.dev.plan;

import com.google.common.math.IntMath;

import java.math.RoundingMode;

public class VariabilityRater {

    public static final int MAX_LEVEL_YIELDING_POINTS = 12;
    public static final int BASE_POINT_VALUE = 1000;
    private static final long[] CUMULATIVE_POINTS_DECAYING = new long[MAX_LEVEL_YIELDING_POINTS + 1];
    static {
        for (int i = 0; i < CUMULATIVE_POINTS_DECAYING.length; i++) {
            CUMULATIVE_POINTS_DECAYING[i] = calculateCumulativePointsDecaying(i);
        }
    }

    private final VariabilityCollector variabilityCollector;

    public VariabilityRater(VariabilityCollector variabilityCollector) {
        this.variabilityCollector = variabilityCollector;
    }

    public long getPoints() {
        return getPointsForCommandExecution() + getPointsForArrowAccess() + getPointsForNewNodes();
    }

    private long getPointsForCommandExecution() {
        return variabilityCollector.commandDataMap.values().stream()
                .map(VariabilityCollector.CommandData::getTimesExecuted)
                .map(VariabilityRater::getCumulativePointsDecaying)
                .mapToLong(Long::longValue)
                .sum();
    }

    private long getPointsForArrowAccess() {
        long readBase = variabilityCollector.arrowDataMap.values().stream()
                .map(VariabilityCollector.ArrowData::getTimesRead)
                .map(VariabilityRater::getCumulativePointsDecaying)
                .mapToLong(Long::longValue)
                .sum();
        long writeBase = variabilityCollector.arrowDataMap.values().stream()
                .map(VariabilityCollector.ArrowData::getTimesWritten)
                .map(VariabilityRater::getCumulativePointsDecaying)
                .mapToLong(Long::longValue)
                .sum();
        return Math.round(0.4 * writeBase) + Math.round(0.2 * readBase);
    }

    private long getPointsForNewNodes() {
        VariabilityCollector.NewNodeData newNodeData = variabilityCollector.newNodeData;
        long sum = getCumulativePointsDecaying(newNodeData.getNumCNode())
                + getCumulativePointsDecaying(newNodeData.getNumUNode())
                + getCumulativePointsDecaying(newNodeData.getNumBNode());
        return Math.round(1.7 * sum);
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
