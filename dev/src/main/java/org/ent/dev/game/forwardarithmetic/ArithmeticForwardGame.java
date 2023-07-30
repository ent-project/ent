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
import org.ent.util.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import static org.ent.util.NetBuilder.builder;
import static org.ent.util.NetBuilder.node;
import static org.ent.util.NetBuilder.unaryRight;
import static org.ent.util.NetBuilder.value;

public class ArithmeticForwardGame {
    public static final TriOperation[] TRI_OPERATIONS = new TriOperation[]{
            Operations.PLUS_OPERATION,
            Operations.MINUS_OPERATION,
            Operations.MULTIPLY_OPERATION,
//            Operations.MODULO_OPERATION,
            Operations.BITWISE_OR_OPERATION,
            Operations.BITWISE_AND_OPERATION,
            Operations.XOR_OPERATION,
//            Operations.ROTATE_RIGHT_OPERATION,
//            Operations.SHIFT_LEFT_OPERATION,
//            Operations.SHIFT_RIGHT_OPERATION
    };

    private static final Logger log = LoggerFactory.getLogger(ArithmeticForwardGame.class);

    private final int operand1;
    private final int operand2;
    private final int operationNodeValue;
    private final TriOperation operation;
    private final Ent ent;
    private int maxSteps;

    private final int expectedSolution;

    private boolean verbose;

    private boolean executionStopped;

    /**
     * "afterStep": trigger a hook/callback after the execution of the current step is completed.
     * To do so, you can submit "afterStepInfo" during the step execution (typically
     * from a listener).
     *
     * If "afterStepInfo" is present, afterStepHook will be called. Finally, the "afterStepInfo"
     * is cleared and the game proceeds to the next execution step.
     */
    private Object afterStepInfo;
    public interface AfterStepHook {
        void afterStep(ArithmeticForwardGame game, Object afterStepInfo);
    }
    private AfterStepHook afterStepHook;

    private NetFormatter formatter;

    private int step;
    private Net verifierNet;
    private Node verifierNetOriginalRoot;
    private Net answerNet;
    private Node answerNode;
    private int verifierPortalCode1, verifierPortalCode2;
    private LazyPortalArrow verifierPortal1, verifierPortal2;
    private PortalArrow answerPortal;
    private Node operationNode, operand1Node, operand2Node, verifierRoot;
    private Consumer<Net> postVerifierCreateHook;

    public enum OpTarget {
        OPERATION, OPERAND1, OPERAND2;
    }

    public ArithmeticForwardGame(int operand1, int operand2, TriOperation operation, Net net, int maxSteps) {
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.operation = operation;
        this.operationNodeValue = Commands.get(this.operation, Accessors.RIGHT, Accessors.LEFT_LEFT, Accessors.LEFT_RIGHT).getValue();
        this.ent = buildEnt(net);
        this.maxSteps = maxSteps;
        this.expectedSolution = calculateExpectedSolution();
    }

    public int getOperand1() {
        return operand1;
    }

    public int getOperand2() {
        return operand2;
    }

    public TriOperation getOperation() {
        return operation;
    }

    public int getOperationNodeValue() {
        return operationNodeValue;
    }

    public int getOpValue(OpTarget target) {
        return switch (target) {
            case OPERAND1 -> operand1;
            case OPERAND2 -> operand2;
            case OPERATION -> operationNodeValue;
        };
    }

    public void setAfterStepHook(AfterStepHook afterStepHook) {
        this.afterStepHook = afterStepHook;
    }

    public void submitAfterStepInfo(Object afterStepInfo) {
        this.afterStepInfo = afterStepInfo;
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
    }

    public LazyPortalArrow getVerifierPortal1() {
        return verifierPortal1;
    }

    public LazyPortalArrow getVerifierPortal2() {
        return verifierPortal2;
    }

    public PortalArrow getAnswerPortal() {
        return answerPortal;
    }

    public Node getVerifierNetOriginalRoot() {
        return verifierNetOriginalRoot;
    }

    public void stopExecution() {
        this.executionStopped = true;
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

    public boolean isVerbose() {
        return verbose;
    }

    public int getStep() {
        return step;
    }

    public Net getVerifierNet() {
        return verifierNet;
    }

    public Net getAnswerNet() {
        return answerNet;
    }

    public Node getAnswerNode() {
        return answerNode;
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
        verifierPortal1 = new LazyPortalArrow(() -> {
            initializeVerifier();
            return new RootPortalArrow(this.verifierNet, this.verifierNetOriginalRoot);
        });
        verifierPortalCode1 = ent.addPortal(verifierPortal1);
        verifierPortal2 = new LazyPortalArrow(() -> {
            initializeVerifier();
            return new RootPortalArrow(this.verifierNet, this.verifierNetOriginalRoot);
        });
        verifierPortalCode2 = ent.addPortal(verifierPortal2);
        return ent;
    }

    public void initializeVerifier() {
        if (this.verifierNet == null) {
            this.verifierNet = buildVerifier();
            this.verifierNetOriginalRoot = verifierNet.getRoot();
        }
    }

    private Net buildVerifier() {
        Node portalAnswer, answerCopy, solution;
        Net verifierNet = builder().net(verifierRoot = node(Commands.get(Operations.SET_VALUE_OPERATION, Accessors.RIGHT, Accessors.LEFT),
                portalAnswer = unaryRight(answerCopy = node()),
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
        answerPortal = new PortalArrow(answerNet);
        int answerPortalCode = ent.addPortal(answerPortal);
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
            Logging.logDot(ent);
        }
        while (step < maxSteps) {
            runner.step();
            if (verbose) {
                dumpResults();
                log.info("after step {}: {}", step, formatter.format(ent));
                Logging.logDot(ent);
            }

            if (afterStepInfo != null) {
                afterStepHook.afterStep(this, afterStepInfo);
                afterStepInfo = null;
            }

            step++;

            if (this.executionStopped) {
                if (verbose) {
                    log.info("execution stopped before step {}", step);
                }
                break;
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

    public boolean passedPortalMoved() {
        if (verifierPortal1.isInitialized()) {
            if (verifierPortal1.getTarget(Purview.DIRECT) != verifierRoot) {
                return true;
            }
        }
        if (verifierPortal2.isInitialized()) {
            if (verifierPortal2.getTarget(Purview.DIRECT) != verifierRoot) {
                return true;
            }
        }
        return false;
    }

    public boolean isVerifierChanged() {
        return verifierNet != null && verifierNet.getRoot() != verifierNetOriginalRoot;
    }
}
