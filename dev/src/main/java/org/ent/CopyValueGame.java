package org.ent;

import org.ent.dev.randnet.DefaultValueDrawing;
import org.ent.dev.randnet.PortalValue;
import org.ent.dev.randnet.RandomNetCreator;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.net.node.cmd.veto.Conditions;
import org.ent.net.node.cmd.veto.Vetos;
import org.ent.run.EntRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import static org.ent.net.node.cmd.accessor.Accessors.FLOW;
import static org.ent.net.node.cmd.accessor.Accessors.LEFT_LEFT;
import static org.ent.net.node.cmd.accessor.Accessors.LEFT_RIGHT;
import static org.ent.net.node.cmd.accessor.Accessors.RIGHT;
import static org.ent.util.NetBuilder.builder;
import static org.ent.util.NetBuilder.ignored;
import static org.ent.util.NetBuilder.node;
import static org.ent.util.NetBuilder.value;

class CopyValueGame {
    private static final Logger log = LoggerFactory.getLogger(CopyValueGame.class);

    public static final DefaultValueDrawing drawing;

    static {
        drawing = new DefaultValueDrawing();
        drawing.addValueBase(new PortalValue(0, 1), DefaultValueDrawing.WEIGHT3);
    }

    private Net verifierNet;
    private final Ent ent;
    private Node targetValueNode;
    private Node inputNode;
    private EntListener entListener;
    VerifierNetListener verifierNetListener;
    InputNetListener inputNetListener;
    private final int targetValue;

    private boolean verbose;

    private NetFormatter formatter;
    private Net inputNet;

    public CopyValueGame(int targetValue, long netCreatorSeed) {
        this.targetValue = targetValue;
        this.ent = buildEnt(netCreatorSeed);
    }

    public CopyValueGame(int targetValue, Net net) {
        this.targetValue = targetValue;
        this.ent = buildEnt(net);
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
        this.formatter = new NetFormatter();
    }

    private Ent buildEnt(long netCreatorSeed1) {
        RandomNetCreator netCreator = new RandomNetCreator(new Random(netCreatorSeed1), drawing);
        return buildEnt(netCreator.drawNet());
    }

    private Ent buildEnt(Net net) {
        Ent ent = new Ent(net);
        ent.setEventListener(entListener = new EntListener());

        ent.addPortal(new LazyPortalArrow(() -> {
            if (verifierNet == null) {
                buildVerifier(targetValue);
            }
            return new PortalArrow(verifierNet);
        }));
        return ent;
    }

    private void buildVerifier(int targetValue) {
        Node data, portalInput;
        verifierNet = builder().net(
                node(Commands.NOP,
                        data = portalInput = node(ignored(), targetValueNode = value(targetValue)),
                        node(Commands.get(Operations.SET_OPERATION, FLOW, RIGHT),
                                node(Vetos.get(Conditions.SAME_VALUE_CONDITION, LEFT_LEFT, LEFT_RIGHT),
                                        data,
                                        value(Commands.FINAL_SUCCESS)),
                                value(Commands.FINAL_FAILURE))));
        verifierNet.setPermittedToWrite(false);
        verifierNet.setPermittedToEvalRoot(true);
        ent.addDomain(verifierNet);
        ent.addPortal(new RootPortalArrow(verifierNet));
        ent.addPortal(new RootPortalArrow(verifierNet));
        verifierNetListener = new VerifierNetListener();
        verifierNet.addEventListener(verifierNetListener);

        inputNet = builder().net(inputNode = value(0));
        inputNet.setPermittedToWrite(true);
        ent.addDomain(inputNet);
        int portalInputCode = ent.addPortal(new PortalArrow(inputNet));
        inputNetListener = new InputNetListener();
        inputNet.addEventListener(inputNetListener);

        portalInput.setValue(portalInputCode);
    }

    public void execute() {
        EntRunner runner = new EntRunner(ent);
        if (verbose) {
            log.info("ent {}", formatter.format(ent));
        }
        for (int s = 0; s < DevelopmentLevel0.MAX_STEPS; s++) {
            runner.step();
            if (verbose) {
                dumpResults();
                log.info("after step {}: {}", s, formatter.format(ent));
            }
        }
    }

    public void dumpResults() {
        if (verifierNetListener != null) {
            if (verifierNetListener.numGetValue > 0) {
                log.info("  verifier access get: {}", verifierNetListener.numGetValue);
                if (verifierNetListener.numGetTargetValue > 0) {
                    log.info("    on target: {}", verifierNetListener.numGetTargetValue);
                }
            }
        }
        if (inputNetListener != null) {
            if (inputNetListener.numSetValue > 0) {
                log.info("  input set: {}", inputNetListener.numSetValue);
                int value = inputNode.getValue(Purview.DIRECT);
                log.info("   (value is now: {} {})", value, value == 0 ? "" : (value == targetValue ? " - TARGET VALUE!" : " - it changed"));
            }
        }
    }

    boolean passedGetTargetValue() {
        return verifierNetListener != null && verifierNetListener.numGetTargetValue > 0;
    }

    boolean passedInputSet() {
        return inputNetListener != null && inputNetListener.numSetValue > 0;
    }

    boolean passedInputSetToTargetValue() {
        if (inputNode != null) {
            int value = inputNode.getValue(Purview.DIRECT);
            return value == targetValue;
        }
        return false;
    }

    private class EntListener extends NopEntEventListener {
    }

    class VerifierNetListener extends NopNetEventListener {
        private int numGetValue;

        int numGetTargetValue;

        @Override
        public void getValue(Node node, Purview purview) {
            numGetValue++;
            if (node == targetValueNode) {
                numGetTargetValue++;
            }
        }
    }

    class InputNetListener extends NopNetEventListener {
        int numSetValue;

        @Override
        public void setValue(Node node, int previousValue, int newValue) {
            numSetValue++;
        }
    }
}
