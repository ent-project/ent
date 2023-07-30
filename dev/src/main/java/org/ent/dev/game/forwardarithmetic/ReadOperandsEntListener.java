package org.ent.dev.game.forwardarithmetic;

import org.ent.NopEntEventListener;
import org.ent.net.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;

public class ReadOperandsEntListener extends NopEntEventListener {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public enum TransferTarget {
        OPERATION, OPERAND1, OPERAND2;
    }

    public class TransferData {
        private final String name;
        public int numTransfer;
        public Integer firstTransfer;
        public Integer lastTransfer;

        public TransferData(String name) {
            this.name = name;
        }

        private void recordTransfer() {
            numTransfer++;
            if (firstTransfer == null) {
                firstTransfer = game.getStep();
            }
            lastTransfer = game.getStep();
            if (game.isVerbose()) {
                log.info("event: {}", name);
            }
        }

        public void dump() {
            if (firstTransfer != null) {
                log.info("{}: first: {}, last:{}, count:{}", name, firstTransfer, lastTransfer, numTransfer);
            }
        }
    }

    private final ArithmeticForwardGame game;
    private final EnumMap<TransferTarget, TransferData> data = new EnumMap<>(TransferTarget.class);
    private final TransferData operand1Data = new TransferData("TransferOperand1");
    private final TransferData operand2Data = new TransferData("TransferOperand2");
    private final TransferData operationData = new TransferData("TransferOperator");

    public ReadOperandsEntListener(ArithmeticForwardGame game) {
        this.game = game;
        data.put(TransferTarget.OPERATION, operationData);
        data.put(TransferTarget.OPERAND1, operand1Data);
        data.put(TransferTarget.OPERAND2, operand2Data);
    }

    public TransferData operand1Data() {
        return operand1Data;
    }

    public TransferData operand2Data() {
        return operand2Data;
    }

    public TransferData operationData() {
        return operationData;
    }

    public TransferData data(TransferTarget target) {
        return data.get(target);
    }

    @Override
    public void transverValue(Node nodeSource, Node nodeTarget) {
        if (nodeTarget.getNet() == game.getVerifierNet()) {
            return;
        }
        if (game.isVerifierChanged()) {
            return;
        }
        if (nodeSource == game.getOperand1Node()) {
            operand1Data.recordTransfer();
        } else if (nodeSource == game.getOperand2Node()) {
            operand2Data.recordTransfer();
        } else if (nodeSource == game.getOperationNode()) {
            operationData.recordTransfer();
        }
    }

    public Integer firstTransfer() {
        return Util.min(operand1Data.firstTransfer, operand2Data.firstTransfer);
    }

    public Integer lastTransfer() {
        return Util.max(operand1Data.lastTransfer, operand2Data.lastTransfer);
    }

}