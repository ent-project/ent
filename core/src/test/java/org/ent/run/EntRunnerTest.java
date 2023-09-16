package org.ent.run;

import org.ent.Ent;
import org.ent.Profile;
import org.ent.listener.EntEventListener;
import org.ent.net.CopyValueGameTestSetup;
import org.ent.net.Net;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.io.parser.NetParser;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.veto.Conditions;
import org.ent.util.Logging;
import org.ent.webui.WebUI;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ent.net.node.cmd.accessor.Accessors.LEFT;
import static org.ent.net.node.cmd.operation.Operations.INC_OPERATION;
import static org.ent.net.node.cmd.veto.Conditions.IDENTICAL_CONDITION;
import static org.ent.util.NetBuilder.*;

class EntRunnerTest {

    private static final boolean WEB_UI = false;

    private static final int STEPS_CUTOFF = 5;

    private final static Logger log = LoggerFactory.getLogger(EntRunnerTest.class);

    private NetParser parser;

    private NetFormatter formatter;

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

    @BeforeEach
    void setUpParserAndFormatter() {
        parser = new NetParser();
        formatter = new NetFormatter()
                .withForceGivenNodeNames(true);
    }

    @AfterAll
    static void webUIKeepRunning() {
        if (WEB_UI) {
            WebUI.loopForever();
        }
    }

    @Test
    void testNetRunner1() throws Exception {
        doTestNetRunner(true, StepResult.ENDLESS_LOOP, Arrays.asList(
                "<::>(params:(@, [#1]), [params])",
                "[params:(a:[#1], a)]"));
    }

    @Test
    void testNetRunner2() throws Exception {
        doTestNetRunner(false, StepResult.SUCCESS, Arrays.asList(
                "A:<//dup>(B:([B], C:[A]), <xn>((A, _b:<//=\\\\>), _b))",
                "D:<xn>((A:<//dup>(B:([(A, C:[A])], C), D), _b:<//=\\\\>), _b)",
                "A:<//dup>(B:([(_b:<//=\\\\>, C:[_b])], C), D:<xn>((_b, A), A))",
                "D:<xn>((_b:<//=\\\\>, A:<//dup>(B:([(_b, C:[_b])], C), D)), A)",
                "_b:<//=\\\\>"));
    }

    @Test
    void testNetRunner3() throws Exception {
        doTestNetRunner(false, StepResult.SUCCESS, Arrays.asList(
                "A:<xn>[a:[A]]",
                "A:<xn>",
                "A:<xn>"));
    }

    @Test
    void testNetRunner4() throws Exception {
        doTestNetRunner(false, StepResult.ENDLESS_LOOP, Arrays.asList(
                "</=\\sl*>(#1(result:<o>, #3), result)",
                "result:#6"));
    }

    private void doTestNetRunner(boolean allowMarker, StepResult expectedFinalResult,
                                 List<String> steps) throws Exception {
        String netStr = steps.get(0);
        NetParser parser = new NetParser();
        if (allowMarker) {
            parser.permitMarkerNodes();
        }
        Net net = parser.parse(netStr);

        NetFormatter formatter = new NetFormatter()
                .withForceGivenNodeNames(true);
        String out0 = formatter.format(net);
        assertThat(out0).isEqualTo(netStr);

        EntRunner runner = new EntRunner(net);

        for (int i = 1; i < steps.size(); i++) {
            StepResult result = runner.step();
            assertThat(result).isEqualTo(StepResult.SUCCESS);

            net.referentialGarbageCollection();

            String netFmt = formatter.format(net);
            assertThat(netFmt).isEqualTo(steps.get(i));
        }

        StepResult actualFinalResult = runner.step();
        assertThat(actualFinalResult).isEqualTo(expectedFinalResult);
    }

    @Test
    void loop() throws Exception {
        Net net = parser.parse("""
                    line01:<^::>(<?//gt/\\?>((i:#0, #5), FIN:[i]), line02);  ~ goto FIN if i > 5
                    line02:<inc>(i, line01);			   		  			 ~ i++; goto start
                """);
        EntRunner runner = new EntRunner(net);

        StepResult result = null;
        for (int i = 1; i < 40; i++) {
            result = runner.step();
            if (result != StepResult.SUCCESS) {
                break;
            }
        }
        assertThat(result).isEqualTo(StepResult.ENDLESS_LOOP);

        net.referentialGarbageCollection();
        assertThat(formatter.format(net)).isEqualTo("FIN:[i:#6]");
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class Veto {

        @Mock
        private EntEventListener listener;

        @Test
        void pass() {
            Node i;
            Ent ent = builder().ent(
                    unary(Commands.get(INC_OPERATION, LEFT),
                            node(Conditions.SAME_VALUE_CONDITION, i = value(3), value(3))));
            ent.addEventListener(listener);
            EntRunner runner = new EntRunner(ent);

            StepResult result = runner.step();

            assertThat(result).isEqualTo(StepResult.SUCCESS);
            assertThat(i.getValue()).isEqualTo(4);
        }

        @Test
        void block() {
            Node i;
            Ent ent = builder().ent(
                    unary(Commands.get(INC_OPERATION, LEFT),
                            node(IDENTICAL_CONDITION, i = value(3), value(3))));
            ent.addEventListener(listener);
            EntRunner runner = new EntRunner(ent);

            StepResult result = runner.step();

            assertThat(result).isEqualTo(StepResult.SUCCESS);
            assertThat(i.getValue()).isEqualTo(3);
        }
    }

    @Nested
    class VerifierGame extends CopyValueGameTestSetup {
        @Test
        void copyValue() {
            build();

            EntRunner runner = new EntRunner(ent);
            Logging.logDot(ent);

            int i = 0;
            while (true) {
                if (Commands.isFinal(ent.getNet())) {
                    break;
                }
                StepResult result = runner.step();
                log.info("after step {}:", i);
                Logging.logDot(ent);
                assertThat(result).isEqualTo(StepResult.SUCCESS);
                assertThat(i++).isLessThan(STEPS_CUTOFF);
            }

            assertThat(verifier.getRoot().getValue()).isEqualTo(Commands.FINAL_SUCCESS.getValue());
            assertThat(inputNode.getValue()).isEqualTo(targetValue);
        }
    }
}
