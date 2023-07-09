package org.ent.dev.game.forwardarithmetic;

import org.assertj.core.api.Assertions;
import org.ent.Profile;
import org.ent.net.Net;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.accessor.Accessors;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.webui.WebUI;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.ent.util.NetBuilder.builder;
import static org.ent.util.NetBuilder.ignored;
import static org.ent.util.NetBuilder.node;
import static org.ent.util.NetBuilder.unary;

class TwoNumberArithmeticForwardGameTest {

    private static boolean WEB_UI = false;

    @BeforeAll
    static void setTestEnvironment() {
        Profile.setTest(true);
    }

    @Test
    void test() {
        if (WEB_UI) {
            WebUI.setUpJavalin();
        }

        Node verifierPortal1, verifierPortal2;
        Node operationCopy, operand1Copy, operand2Copy, operandsCopy, solutionTarget;
        Node operationCopyContinuation;
        Net net = builder().net(
                // copy operation
                node(Commands.get(Operations.SET_VALUE_OPERATION, Accessors.RIGHT, Accessors.LEFT_RIGHT),
                        verifierPortal1 = node(
                                ignored(),
                                operationCopy = unary(node(operandsCopy = node(operand1Copy = node(), operand2Copy = node()), solutionTarget = node()))),
                        // point second verifier portal deeper into the verifier
                        node(Commands.get(Operations.SET_OPERATION, Accessors.LEFT, Accessors.LEFT_RIGHT_LEFT),
                                verifierPortal2 = node(ignored(), operandsCopy),
                                // copy first operand
                                node(Commands.get(Operations.SET_VALUE_OPERATION, Accessors.RIGHT_LEFT, Accessors.LEFT_LEFT_LEFT),
                                        verifierPortal2,
                                        // copy second operand
                                        node(Commands.get(Operations.SET_VALUE_OPERATION, Accessors.RIGHT_RIGHT, Accessors.LEFT_LEFT_RIGHT),
                                                verifierPortal2,
                                                // evaluate the copied operation
                                                operationCopy)))),
                operationCopyContinuation =
                        // submit the evaluated answer
                        node(Commands.get(Operations.SET_VALUE_OPERATION, Accessors.LEFT_LEFT_LEFT, Accessors.RIGHT_LEFT_RIGHT),
                                verifierPortal1,
                                node(Commands.get(Operations.EVAL_FLOW_OPERATION, Accessors.LEFT),
                                        verifierPortal1,
                                        node(Commands.get(Operations.EVAL_FLOW_OPERATION, Accessors.LEFT),
                                                verifierPortal1,
                                                node(Commands.get(Operations.EVAL_FLOW_OPERATION, Accessors.LEFT),
                                                        verifierPortal1,
                                                        node(Commands.FINAL_SUCCESS))))
                        ));

        operationCopy.setRightChild(operationCopyContinuation);


        TwoNumberArithmeticForwardGame game = new TwoNumberArithmeticForwardGame(7, 3, Operations.MULTIPLY_OPERATION, net, 15);

        verifierPortal1.setValue(game.getVerifierPortalCode1());
        verifierPortal2.setValue(game.getVerifierPortalCode2());

        game.setVerbose(true);
        game.execute();

        Assertions.assertThat(game.passedVerifierFinished()).isTrue();
        Assertions.assertThat(game.passedVerifierFinishedSuccessfully()).isTrue();

        if (WEB_UI) {
            WebUI.loopForever();
        }
    }

}