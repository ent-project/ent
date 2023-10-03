package org.ent.dev.game.enrich;

import org.ent.net.Net;
import org.ent.net.node.Node;
import org.ent.net.util.NetUtils;
import org.ent.permission.Permissions;

import java.util.HashSet;
import java.util.Set;

/**
 * StaticValuation is a rough but quick check to see if the net has any interesting features.
 * <p>
 * Looks at all nodes, possibly including unreachable. This means, a better valuation
 * has to be done in a second stage in case the net is deemed interesting.
 */
public class StaticValuation {

    public static double calculateFitness(Net net) {
        double fitness = 0;
        // it is good, if the value of the root node has been changed from default (0)
        Node root = net.getRoot();
        if (root.getValue(Permissions.DIRECT) != 0) {
            fitness += 0.1;
        }

        Set<Node> reachableNodes = NetUtils.collectReachable(net.getRoot());

        // having many different values is good
        fitness += limitFunction(differentValues(reachableNodes), 5);

        // having many nodes is good (up to a point)
        fitness += 10 * limitFunction(reachableNodes.size(), 10);

        return fitness;
    }

    private static int differentValues(Set<Node> nodes) {
        HashSet<Integer> values = new HashSet<>(initialCapacity(nodes.size()));
        for (Node node : nodes) {
            values.add(node.getValue(Permissions.DIRECT));
        }
        return values.size();
    }

    private static int initialCapacity(int size) {
        return (int) (size / 0.75 + 1);
    }

    /**
     * Limit function that maps a non-negative integer into the interval 0..1
     * <p>
     * Starts at the origin and steadily increases, but does not exceed the maximum value 1.
     * At value = halfMaximumValue, the function has the value 0.5.
     * At the origin, the function has a slope of (1.0 / halfMaximumValue).
     */
    public static double limitFunction(int value, double halfMaximumValue) {
        if (value <= 0) {
            return 0.0;
        }
        return 1.0 - 1.0 / (value / halfMaximumValue + 1.0);
    }
}