package org.ent.dev.game.forwardarithmetic.readinfo;

import org.ent.NopEntEventListener;
import org.ent.dev.game.forwardarithmetic.ArithmeticForwardGame;
import org.ent.dev.game.forwardarithmetic.Util;
import org.ent.net.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadOperandsEntListener extends NopEntEventListener {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ArithmeticForwardGame game;
    int numTransferOperand1BeforeEval;
    int numTransferOperand2BeforeEval;
    Integer firstTransferOperand1;
    Integer firstTransferOperand2;
    Integer lastTransferOperand1;
    Integer lastTransferOperand2;

    ReadOperandsEntListener(ArithmeticForwardGame game) {
        this.game = game;
    }

    @Override
    public void transverValue(Node nodeSource, Node nodeTarget) {
        if (nodeTarget.getNet() == game.getVerifierNet()) {
            return;
        }
        if (game.isVerifierExecuted()) {
            return;
        }
        if (nodeSource == game.getOperand1Node()) {
            numTransferOperand1BeforeEval++;
            if (firstTransferOperand1 == null) {
                firstTransferOperand1 = game.getStep();
            }
            lastTransferOperand1 = game.getStep();
            if (game.isVerbose()) {
                log.info("TransferOperand1");
            }
        }
        if (nodeSource == game.getOperand2Node()) {
            numTransferOperand2BeforeEval++;
            if (firstTransferOperand2 == null) {
                firstTransferOperand2 = game.getStep();
            }
            lastTransferOperand2 = game.getStep();
            if (game.isVerbose()) {
                log.info("TransferOperand2");
            }
        }
    }

    public Integer firstTransfer() {
        return Util.min(firstTransferOperand1, firstTransferOperand2);
    }

    public Integer lastTransfer() {
        return Util.max(lastTransferOperand1, lastTransferOperand2);
    }

}
