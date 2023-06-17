package org.ent;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.ent.net.Purview;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Command;
import org.ent.net.node.cmd.veto.Veto;
import org.ent.run.StepResult;

public class StepObservation extends NopEntEventListener {

    public static class StepData {

        int commandNodeValue;
        int commandNodeIndex;

        StepResult stepResult;

        Veto veto;
        long vetoNode1; // TODO: check domain crossing
        long vetoNode2;
        boolean vetoPass;
    }

    public static final int MURMUR_SEED = 0x234f3a;

    private final Hasher hasher;

    private StepData stepData;

    public StepObservation() {
        HashFunction murmur3 = Hashing.murmur3_32_fixed(MURMUR_SEED);
        this.hasher = murmur3.newHasher();
    }

    public int getHashValue() {
        HashCode hash = hasher.hash();
        return hash.asInt();
    }

    @Override
    public void beforeCommandExecution(Node executionPointer, Command command) {
        hasher.putLong(executionPointer.getAddress());
        hasher.putInt(command.getValue());

        stepData = new StepData();
        stepData.commandNodeValue = executionPointer.getValue(Purview.DIRECT);
        stepData.commandNodeIndex = executionPointer.getIndex();
    }

    @Override
    public void afterCommandExecution(StepResult stepResult) {
        hasher.putInt(stepResult.ordinal());
        stepData.stepResult = stepResult;
    }

    @Override
    public void vetoEvaluation(Veto veto, Node node1, Node node2, boolean result) {
        stepData.veto = veto;
        stepData.vetoNode1 = node1.getAddress();
        stepData.vetoNode2 = node2.getAddress();
        stepData.vetoPass = result;
    }
}
