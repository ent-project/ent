package org.ent.dev.game.forwardarithmetic;

import org.apache.commons.rng.UniformRandomProvider;
import org.ent.Ent;
import org.ent.LazyPortalArrow;
import org.ent.PortalArrow;
import org.ent.RootPortalArrow;
import org.ent.net.Net;
import org.ent.net.Purview;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.accessor.Accessors;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.net.node.cmd.operation.TriOperation;
import org.ent.net.node.cmd.operation.TriValueOperation;
import org.ent.net.node.cmd.operation.math.ModuloOperation;
import org.ent.net.node.cmd.veto.Conditions;
import org.ent.net.node.cmd.veto.Vetos;
import org.ent.run.EntRunner;
import org.ent.util.DotLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import static org.ent.util.NetBuilder.builder;
import static org.ent.util.NetBuilder.ignored;
import static org.ent.util.NetBuilder.node;
import static org.ent.util.NetBuilder.value;

public class ArithmeticForwardGame {
    public static final TriOperation[] TRI_OPERATIONS = new TriOperation[]{
            Operations.PLUS_OPERATION,
            Operations.MINUS_OPERATION,
            Operations.MULTIPLY_OPERATION,
            Operations.MODULO_OPERATION,
            Operations.BITWISE_OR_OPERATION,
            Operations.BITWISE_AND_OPERATION,
            Operations.XOR_OPERATION,
            Operations.ROTATE_RIGHT_OPERATION,
            Operations.SHIFT_LEFT_OPERATION,
            Operations.SHIFT_RIGHT_OPERATION
    };

    private static final Logger log = LoggerFactory.getLogger(ArithmeticForwardGame.class);

    private final int operand1;
    private final int operand2;
    private final TriOperation operation;
    private final Ent ent;
    private final int maxSteps;

    private final int expectedSolution;

    private boolean verbose;
    private NetFormatter formatter;

    private Net verifierNet;
    private Node verifierNetOriginalRoot;
    private Net answerNet;
    private Node answerNode;
    private int verifierPortalCode1;
    private int verifierPortalCode2;
    private Node operationNode;
    private Node operand1Node;
    private Node operand2Node;

    private Consumer<Net> postVerifierCreateHook;

    public ArithmeticForwardGame(int operand1, int operand2, TriOperation operation, Net net, int maxSteps) {
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.operation = operation;
        this.ent = buildEnt(net);
        this.maxSteps = maxSteps;
        this.expectedSolution = calculateExpectedSolution();
    }

    private int calculateExpectedSolution() {
        if (operation instanceof TriValueOperation valueOperation) {
            return valueOperation.compute(operand1, operand2);
        } else if (operation instanceof ModuloOperation modulo) {
            return modulo.compute(operand1, operand2);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void setPostVerifierCreateCallback(Consumer<Net> postVerifierCreateHook) {
        this.postVerifierCreateHook = postVerifierCreateHook;
    }

    public Node getOperationNode() {
        return operationNode;
    }

    public Node getOperand1Node() {
        return operand1Node;
    }

    public Node getOperand2Node() {
        return operand2Node;
    }

    public int getExpectedSolution() {
        return expectedSolution;
    }

    public int getVerifierPortalCode1() {
        return verifierPortalCode1;
    }

    public int getVerifierPortalCode2() {
        return verifierPortalCode2;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
        this.formatter = new NetFormatter();
    }

    public Net getVerifierNet() {
        return verifierNet;
    }

    public Ent getEnt() {
        return ent;
    }

    public static TriOperation drawOperation(UniformRandomProvider random) {
        return TRI_OPERATIONS[random.nextInt(TRI_OPERATIONS.length)];
    }

    public static int drawOperand(UniformRandomProvider random) {
        // "modulo" operation: second operand should not be 0 -> some lower bound > 0
        return random.nextInt(3, 100);
    }

    private Ent buildEnt(Net net) {
        Ent ent = new Ent(net);
        verifierPortalCode1 = ent.addPortal(new LazyPortalArrow(() -> {
            if (this.verifierNet == null) {
                this.verifierNet = buildVerifier();
                this.verifierNetOriginalRoot = verifierNet.getRoot();
            }
            return new RootPortalArrow(this.verifierNet, this.verifierNetOriginalRoot);
        }));
        verifierPortalCode2 = ent.addPortal(new LazyPortalArrow(() -> {
            if (this.verifierNet == null) {
                this.verifierNet = buildVerifier();
                this.verifierNetOriginalRoot = verifierNet.getRoot();
            }
            return new RootPortalArrow(this.verifierNet, this.verifierNetOriginalRoot);
        }));
        return ent;
    }

    private Net buildVerifier() {
        Node portalAnswer, answerCopy, solution;
        Net verifierNet = builder().net(node(Commands.get(Operations.SET_VALUE_OPERATION, Accessors.RIGHT, Accessors.LEFT),
                portalAnswer = node(ignored(), answerCopy = node()),
                operationNode = node(Commands.get(this.operation, Accessors.RIGHT, Accessors.LEFT_LEFT, Accessors.LEFT_RIGHT),
                        node(
                                node(operand1Node = value(this.operand1), operand2Node = value(this.operand2)),
                                solution = node()
                        ),
                        node(Commands.get(Operations.SET_OPERATION, Accessors.FLOW, Accessors.RIGHT),
                                node(Vetos.get(Conditions.SAME_VALUE_CONDITION, Accessors.LEFT_LEFT, Accessors.LEFT_RIGHT),
                                        node(answerCopy, solution),
                                        value(Commands.FINAL_SUCCESS)),
                                value(Commands.FINAL_FAILURE)))));
        verifierNet.setPermittedToWrite(false);
        verifierNet.setPermittedToEvalRoot(true);
        verifierNet.setName("verifier");
        ent.addDomain(verifierNet);

        answerNet = builder().net(answerNode = value(0));
        answerNet.setPermittedToWrite(true);
        answerNet.setName("answer");
        ent.addDomain(answerNet);
        int answerPortalCode = ent.addPortal(new PortalArrow(answerNet));
        portalAnswer.setValue(answerPortalCode);

        if (postVerifierCreateHook != null) {
            postVerifierCreateHook.accept(verifierNet);
        }

        return verifierNet;
    }

    public void execute() {
        EntRunner runner = new EntRunner(ent);
        if (verbose) {
            log.info("ent {}", formatter.format(ent));
            DotLogger.log(ent);
        }
        for (int step = 0; step < maxSteps; step++) {
            runner.step();
            if (verbose) {
                dumpResults();
                log.info("after step {}: {}", step, formatter.format(ent));
                DotLogger.log(ent);
            }
        }
    }

    private void dumpResults() {

    }


    public boolean passedVerifierFinished() {
        return verifierNet != null &&
                (verifierNet.getRoot().getValue(Purview.DIRECT) == Commands.FINAL_SUCCESS.getValue() ||
                        verifierNet.getRoot().getValue(Purview.DIRECT) == Commands.FINAL_FAILURE.getValue());
    }

    public boolean passedVerifierFinishedSuccessfully() {
        return verifierNet != null &&
                verifierNet.getRoot().getValue(Purview.DIRECT) == Commands.FINAL_SUCCESS.getValue();
    }

}
