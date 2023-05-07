package org.ent.run;

import org.ent.net.Net;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.io.parser.NetParser;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NetRunnerTest {

	@Test
	void testNetRunner1() throws Exception {
		doTestNetRunner(true, StepResult.ENDLESS_LOOP, Arrays.asList(
				"<=>(params:(@, [#1]), [params])",
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
                .withAscii(true)
                .withNodeNamesInverse(parser.getNodeNames())
                .withForceGivenNodeNames(true);
		String out0 = formatter.format(net);
		assertThat(out0).isEqualTo(netStr);

		NetRunner runner = new NetRunner(net);

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
}
