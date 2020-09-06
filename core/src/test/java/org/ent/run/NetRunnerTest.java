package org.ent.run;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.ent.net.DefaultNetController;
import org.ent.net.Net;
import org.ent.net.NetController;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.io.parser.NetParser;
import org.ent.net.node.MarkerNode;
import org.junit.jupiter.api.Test;

class NetRunnerTest {

	@Test
	void testNetRunner1() throws Exception {
		doTestNetRunner(true, StepResult.FATAL, Arrays.asList(
				"((<eval1>, (<\\==|>, [#])), A=(B=(<\\==|>, (A, [B])), [#]))",
				"A=(B=(<\\==|>, (A, [B])), [#])",
				"a=[B=(<\\==|>, ((B, a), a))]"));
	}

	@Test
	void testNetRunner2() throws Exception {
		doTestNetRunner(false, StepResult.FATAL, Arrays.asList(
				"A=((_a=<|dup*>, B=([B], C=(_a, C))), ((<ix>, (A, _b=<eval5>)), _b))",
				"D=((<ix>, (((_a=<|dup*>, ([(_a, C=(_a, C))], C)), D), _b=<eval5>)), _b)",
				"A=((_a=<|dup*>, ([(_a, C=(_a, C))], C)), ((<ix>, (<eval5>, A)), A))",
				"D=((<ix>, (<eval5>, A=((_a=<|dup*>, ([(_a, C=(_a, C))], C)), D))), A)",
				"<eval5>"));
	}

	@Test
	void testNetRunner3() throws Exception {
		doTestNetRunner(false, StepResult.INVALID_COMMAND_NODE, Arrays.asList(
				"A=(B=(<ix>, B), (A, B))",
				"C=((_a=<ix>, C), _a)",
				"A=(A, (<ix>, A))"));
	}

	@Test
	void testNetRunner4() throws Exception {
		doTestNetRunner(false, StepResult.FATAL, Arrays.asList(
				"(A=(_a=</dup*>, B=((C=(A, a=[([(((</dup*>, a), [_b=</:\\\\>]), _a)], _b)]), B), _a)), C)",
				"((_a=</dup*>, B=((</dup*>, B), _a)), a=[([(((</dup*>, a), [_b=</:\\\\>]), _a)], _b)])",
				"a=[([(((</dup*>, a), [_b=</:\\\\>]), </dup*>)], _b)]"));
	}

	private void doTestNetRunner(boolean allowMarker, StepResult expectedFinalResult,
			List<String> steps) throws Exception {
		String netStr = steps.get(0);
		NetParser parser = new NetParser();
		if (allowMarker) {
			MarkerNode marker = new MarkerNode();
			parser.permitMarkerNodes(marker);
		}
		Net net = parser.parse(netStr);

		NetFormatter formatter = new NetFormatter().withAscii(true);
		String out0 = formatter.format(net);
		assertThat(out0).isEqualTo(netStr);

		NetController controller = new DefaultNetController(net);
		NetRunner runner = new NetRunner(net, controller);

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
