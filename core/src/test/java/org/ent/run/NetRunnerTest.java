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

public class NetRunnerTest {

	@Test
	public void testNetRunner1() throws Exception {
		doTestNetRunner(true, Arrays.asList(
				"((<eval1>, (<\\==|>, [#])), A=(B=(<\\==|>, (A, [B])), [#]))",
				"A=(B=(<\\==|>, (A, [B])), [#])",
				"a=[B=(<\\==|>, ((B, a), a))]"));
	}

	@Test
	public void testNetRunner2() throws Exception {
		doTestNetRunner(false, Arrays.asList(
				"A=((_a=<|dup*>, B=([B], C=(_a, C))), ((<ix>, (A, _b=<eval5>)), _b))",
				"D=((<ix>, (((_a=<|dup*>, ([(_a, C=(_a, C))], C)), D), _b=<eval5>)), _b)",
				"A=((_a=<|dup*>, ([(_a, C=(_a, C))], C)), ((<ix>, (<eval5>, A)), A))",
				"D=((<ix>, (<eval5>, A=((_a=<|dup*>, ([(_a, C=(_a, C))], C)), D))), A)",
				"<eval5>"));
	}

	private void doTestNetRunner(boolean allowMarker, List<String> steps) throws Exception {
		String netStr = steps.get(0);
		NetParser parser = new NetParser();
		if (allowMarker) {
			MarkerNode marker = new MarkerNode();
			parser.permitMarkerNodes(marker);
		}
		Net net = parser.parse(netStr);

		NetFormatter formatter = new NetFormatter();
		if (allowMarker) {
			formatter.setMarkerNodesPermitted(true);
		}
		formatter.setAscii(true);
		String out0 = formatter.format(net);
		assertThat(out0).isEqualTo(netStr);

		NetController controller = new DefaultNetController(net);
		NetRunner runner = new NetRunner(net, controller);

		for (int i = 1; i < steps.size(); i++) {
			StepResult r1 = runner.step();
			assertThat(r1).isEqualTo(StepResult.SUCCESS);

			net.referentialGarbageCollection();

			String out1 = formatter.format(net);
			assertThat(out1).isEqualTo(steps.get(i));
		}

		StepResult r3 = runner.step();
		assertThat(r3).isEqualTo(StepResult.FATAL);
	}
}
