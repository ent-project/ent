package org.ent.util;

import org.ent.Ent;
import org.ent.Profile;
import org.ent.net.Net;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.accessor.Accessors;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.net.node.cmd.veto.Conditions;
import org.ent.net.node.cmd.veto.Vetos;
import org.ent.webui.WebUI;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import static org.ent.util.NetBuilder.builder;
import static org.ent.util.NetBuilder.ignored;
import static org.ent.util.NetBuilder.node;
import static org.ent.util.NetBuilder.unary;
import static org.ent.util.NetBuilder.unaryRight;
import static org.ent.util.NetBuilder.value;

class DotRendererTest {

    private static final boolean WEB_UI = false;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @BeforeAll
    static void setTestEnvironment() {
        Profile.setTest(true);
    }

    @BeforeAll
    static void webUI() {
        if (WEB_UI) {
            WebUI.setUpJavalin();
        }
    }

    @AfterAll
    static void webUIKeepRunning() {
        if (WEB_UI) {
            WebUI.loopForever();
        }
    }

    @ParameterizedTest
    @MethodSource("render")
    void render(Ent entInput, TestInfo info) {
        String rendered = new DotRenderer(entInput).render();

        log.info("{}:", info.getDisplayName());
        Logging.dotLogger.info(rendered);
        log.info(rendered);
    }

    private static Stream<Ent> render() {
        Ent ent2 = entBase();
        ent2.getNet().getRoot().setValue(Commands.get(Operations.BITWISE_OR_OPERATION, Accessors.L, Accessors.LL, Accessors.LR).getValue());
        Ent ent3 = entBase();
        ent3.getNet().getRoot().setValue(Commands.get(Operations.PLUS_OPERATION, Accessors.LLL, Accessors.LLR, Accessors.R).getValue());
        Ent entTripleTargetCommand = entBase();
        entTripleTargetCommand.getNet().getRoot().setValue(Commands.get(Operations.PLUS_OPERATION, Accessors.L, Accessors.L, Accessors.L).getValue());
        Ent entTripleTargetVeto = entBase();
        entTripleTargetVeto.getNet().getRoot().setValue(Commands.get(Operations.PLUS_OPERATION, Accessors.LL, Accessors.LL, Accessors.LL).getValue());
        Ent entTripleTargetLeafNumber = entBase();
        entTripleTargetLeafNumber.getNet().getRoot().setValue(Commands.get(Operations.PLUS_OPERATION, Accessors.LLL, Accessors.LLL, Accessors.LLL).getValue());
        Ent entTripleTargetInnerNumber = entBase();
        entTripleTargetInnerNumber.getNet().getRoot().setValue(Commands.get(Operations.PLUS_OPERATION, Accessors.LLR, Accessors.LLR, Accessors.LLR).getValue());
        Ent entTripleTargetDot = entBase();
        entTripleTargetDot.getNet().getRoot().setValue(Commands.get(Operations.PLUS_OPERATION, Accessors.LLRL, Accessors.LLRL, Accessors.LLRL).getValue());
        return Stream.of(entBase(), ent2, ent3, entTripleTargetCommand, entTripleTargetVeto,
                entTripleTargetLeafNumber, entTripleTargetInnerNumber, entTripleTargetDot);
    }

    private static Ent entBase() {
        Ent ent = builder().ent(node(
                unary(Commands.get(Operations.BITWISE_AND_OPERATION, Accessors.LR, Accessors.LLR, Accessors.R),
                        node(Vetos.get(Conditions.GREATER_THAN_CONDITION, Accessors.LR, Accessors.LRLR),
                                value(54),
                                unary(0xa3, ignored()))
                        ),
                value(Commands.CONCLUSION_SUCCESS)));
        Net domain = builder().net(ignored());
        domain.setName("domain");
        ent.addDomain(domain);
        return ent;
    }

    @ParameterizedTest
    @MethodSource("renderSpecialCases")
    void renderSpecialCases(Ent entInput, TestInfo info) {
        String rendered = new DotRenderer(entInput).render();

        log.info("{}:", info.getDisplayName());
        Logging.dotLogger.info(rendered);
        log.info(rendered);
    }

    private static Stream<Ent> renderSpecialCases() {
        Ent targetArrowToSelf = builder().ent(unaryRight(Commands.get(Operations.SET_OPERATION, Accessors.L, Accessors.L),
                ignored()));
        return Stream.of(targetArrowToSelf);
    }
}