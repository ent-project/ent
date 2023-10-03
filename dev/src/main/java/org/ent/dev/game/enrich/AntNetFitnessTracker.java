package org.ent.dev.game.enrich;

import org.ent.listener.NopNetEventListener;
import org.ent.net.Arrow;
import org.ent.net.ArrowDirection;
import org.ent.net.node.Node;
import org.ent.permission.Permissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

class AntNetFitnessTracker extends NopNetEventListener {
    private final static Logger log = LoggerFactory.getLogger(AntNetFitnessTracker.class);

    private final boolean verbose;
    Set<Integer> valuesWritten = new HashSet<>();
    Set<Integer> nodesWithValuesWritten = new HashSet<>();
    Set<Integer> arrowsSet = new HashSet<>();
    Set<Long> arrowsSetTo = new HashSet<>();
    int numNewNodes;

    public AntNetFitnessTracker(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public void setValue(Node node, int previousValue, int newValue) {
        if (newValue != previousValue) {
            valuesWritten.add(newValue);
            nodesWithValuesWritten.add(node.getIndex());
        }
    }

    @Override
    public void calledSetChild(Node from, ArrowDirection arrowDirection, Node to) {
        Arrow arrow = from.getArrow(arrowDirection);
        if (arrow.getTarget(Permissions.DIRECT) != to) {
            int arrowIndex = arrow.getIndex();
            arrowsSet.add(arrowIndex);

            long targetIndex = to.getIndex();
            long arrowAndTargetEncoded = (targetIndex << 32) | arrowIndex;
            arrowsSetTo.add(arrowAndTargetEncoded);
        }
    }

    @Override
    public void calledNewNode(Node n) {
        numNewNodes++;
    }

    public double getFitness() {
        double diffValuesWritten = StaticValuation.limitFunction(valuesWritten.size(), 20.);
        double arrowsSetTo = StaticValuation.limitFunction(this.arrowsSetTo.size(), 15.);
        double newNodes = StaticValuation.limitFunction(numNewNodes, 12.);

        if (verbose) log.info("different values written: {}, different arrows set to something: {}, new nodes: {}",
                diffValuesWritten, arrowsSetTo, newNodes);
        return diffValuesWritten + 3 * arrowsSetTo + 7 * newNodes;
    }
}
