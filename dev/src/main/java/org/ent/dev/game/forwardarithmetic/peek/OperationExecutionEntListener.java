package org.ent.dev.game.forwardarithmetic.peek;

import org.ent.NopEntEventListener;
import org.ent.dev.game.forwardarithmetic.ArithmeticForwardGame;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.operation.TriValueOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationExecutionEntListener extends NopEntEventListener {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final ArithmeticForwardGame game;
    public Integer found;
    public Node rootWhenFound;

    OperationExecutionEntListener(ArithmeticForwardGame game) {
        this.game = game;
    }

    @Override
    public void triValueOperation(Node nodeTarget, Node nodeOperand1, Node nodeOperand2, TriValueOperation operation) {
        if (operation == game.getOperation()) {
            int op1 = nodeOperand1.getValue(Purview.DIRECT);
            int op2 = nodeOperand2.getValue(Purview.DIRECT);
            boolean fullmatch = false;
            if (op1 == game.getOperand1() && op2 == game.getOperand2()) {
                if (game.isVerbose()) {
                    log.info("event: fullmatch");
                }
                fullmatch = true;
            }
            if (op1 == game.getOperand2() && op2 == game.getOperand1()) {
                if (game.isVerbose()) {
                    log.info("event: fullmatch (switched)");
                }
                fullmatch = true;
            }
            if (fullmatch) {
                found = game.getStep();
                rootWhenFound = game.getEnt().getNet().getRoot();
                game.stopExecution();
            }
        }
    }
}
