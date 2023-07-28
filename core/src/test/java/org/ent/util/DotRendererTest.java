package org.ent.util;

import org.ent.Ent;
import org.ent.PortalArrow;
import org.ent.Profile;
import org.ent.net.Net;
import org.ent.net.node.Node;
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
        ent2.getNet().getRoot().setValue(Commands.get(Operations.BITWISE_OR_OPERATION, Accessors.DIRECT, Accessors.LEFT, Accessors.RIGHT).getValue());
        Ent ent3 = entBase();
        ent3.getNet().getRoot().setValue(Commands.get(Operations.PLUS_OPERATION, Accessors.LEFT_LEFT, Accessors.LEFT_RIGHT, Accessors.FLOW).getValue());
        Ent entTripleTargetCommand = entBase();
        entTripleTargetCommand.getNet().getRoot().setValue(Commands.get(Operations.PLUS_OPERATION, Accessors.DIRECT, Accessors.DIRECT, Accessors.DIRECT).getValue());
        Ent entTripleTargetVeto = entBase();
        entTripleTargetVeto.getNet().getRoot().setValue(Commands.get(Operations.PLUS_OPERATION, Accessors.LEFT, Accessors.LEFT, Accessors.LEFT).getValue());
        Ent entTripleTargetPortal = entBase();
        entTripleTargetPortal.getNet().getRoot().setValue(Commands.get(Operations.PLUS_OPERATION, Accessors.RIGHT, Accessors.RIGHT, Accessors.RIGHT).getValue());
        Ent entTripleTargetNumber = entBase();
        entTripleTargetNumber.getNet().getRoot().setValue(Commands.get(Operations.PLUS_OPERATION, Accessors.LEFT_LEFT, Accessors.LEFT_LEFT, Accessors.LEFT_LEFT).getValue());
        Ent entTripleTargetDot = entBase();
        entTripleTargetDot.getNet().getRoot().setValue(Commands.get(Operations.PLUS_OPERATION, Accessors.LEFT_RIGHT, Accessors.LEFT_RIGHT, Accessors.LEFT_RIGHT).getValue());
        return Stream.of(entBase(), ent2, ent3, entTripleTargetCommand, entTripleTargetVeto,
                entTripleTargetPortal, entTripleTargetNumber, entTripleTargetDot);
    }

    private static Ent entBase() {
        Node portal, portalLeft;
        Ent ent = builder().ent(node(
                node(Commands.get(Operations.EVAL_OPERATION, Accessors.RIGHT),
                        node(Vetos.get(Conditions.GREATER_THAN_CONDITION, Accessors.RIGHT, Accessors.RIGHT_LEFT_RIGHT),
                                value(54),
                                ignored()),
                        portal = unary(
                                portalLeft = ignored()
                        )),
                value(Commands.FINAL_SUCCESS)));
        Net domain = builder().net(ignored());
        domain.setName("domain");
        ent.addDomain(domain);
        int portalCode1 = ent.addPortal(new PortalArrow(domain));
        int portalCode2 = ent.addPortal(new PortalArrow(domain));
        portal.setValue(portalCode1 | (portalCode2 << 16));
        portalLeft.setValue(portalCode1);
        return ent;
    }
}