package org.ent.run;

import org.ent.Ent;
import org.ent.EntEventListener;
import org.ent.PortalArrow;
import org.ent.RootPortalArrow;
import org.ent.net.Net;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.io.parser.NetParser;
import org.ent.net.node.Node;
import org.ent.net.node.cmd.Commands;
import org.ent.net.node.cmd.operation.Operations;
import org.ent.net.node.cmd.veto.Conditions;
import org.ent.net.node.cmd.veto.Vetos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ent.net.node.cmd.accessor.Accessors.FLOW;
import static org.ent.net.node.cmd.accessor.Accessors.LEFT;
import static org.ent.net.node.cmd.accessor.Accessors.LEFT_LEFT;
import static org.ent.net.node.cmd.accessor.Accessors.LEFT_LEFT_LEFT;
import static org.ent.net.node.cmd.accessor.Accessors.LEFT_LEFT_RIGHT;
import static org.ent.net.node.cmd.accessor.Accessors.LEFT_RIGHT;
import static org.ent.net.node.cmd.accessor.Accessors.RIGHT;
import static org.ent.net.node.cmd.operation.Operations.INC_OPERATION;
import static org.ent.net.node.cmd.veto.Conditions.IDENTICAL_CONDITION;
import static org.ent.util.NetBuilder.builder;
import static org.ent.util.NetBuilder.ignored;
import static org.ent.util.NetBuilder.node;
import static org.ent.util.NetBuilder.unary;
import static org.ent.util.NetBuilder.value;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class EntRunnerTest {

	private static final int STEPS_CUTOFF = 5;
	private NetParser parser;

	private NetFormatter formatter;

	@BeforeEach
	void setUpParserAndFormatter() {
		parser = new NetParser();
		formatter = new NetFormatter()
				.withForceGivenNodeNames(true);
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
			verify(listener).passedThroughVeto(any());
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
			verify(listener).blockedByVeto(any());
		}
	}

	@Nested
	class VerifierGame {
		@Test
		void copyValue() {
			int targetValue = 7;
			Node verifierPortal;
			Ent ent = builder().ent(
					node(Commands.get(Operations.SET_VALUE_OPERATION, LEFT_LEFT_LEFT, LEFT_LEFT_RIGHT),
							verifierPortal = ignored(),
							node(Commands.get(Operations.EVAL_FLOW_OPERATION, LEFT),
									verifierPortal,
									node(Commands.get(Operations.EVAL_FLOW_OPERATION, LEFT),
											verifierPortal,
											value(Commands.FINAL_SUCCESS)))));
			Node x;
			Net input = builder().net(x = value(0));
			input.setPermittedToWrite(true);
			ent.addDomain(input);
			int portalInputCode = ent.addPortal(new PortalArrow(input));
			Node data;
			Net verifier = builder().net(
					node(Commands.NOP,
							data = node(portalInputCode, ignored(), value(targetValue)),
							node(Commands.get(Operations.SET_OPERATION, FLOW, RIGHT),
									node(Vetos.get(Conditions.SAME_VALUE_CONDITION, LEFT_LEFT, LEFT_RIGHT),
											data,
											value(Commands.FINAL_SUCCESS)),
									value(Commands.FINAL_FAILURE))));
			verifier.setPermittedToWrite(false);
			verifier.setPermittedToEvalRoot(true);
			ent.addDomain(verifier);
			RootPortalArrow rootPortalVerifier = new RootPortalArrow(verifier);
			int portalVerifierCode = ent.addPortal(rootPortalVerifier);
			verifierPortal.setValue(portalVerifierCode);
			EntRunner runner = new EntRunner(ent);

			int i = 0;
			while (true) {
				if (ent.getNet().getRoot().getValue() == Commands.FINAL_SUCCESS.getValue()) {
					break;
				}
				StepResult result = runner.step();
				assertThat(i++).isLessThan(STEPS_CUTOFF);
			}

			assertThat(verifier.getRoot().getValue()).isEqualTo(Commands.FINAL_SUCCESS.getValue());
			assertThat(x.getValue()).isEqualTo(targetValue);
		}
	}
}
