package org.ent.run;

import static org.assertj.core.api.Assertions.assertThat;

import org.ent.net.DefaultNetController;
import org.ent.net.Net;
import org.ent.net.NetController;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.io.parser.NetParser;
import org.ent.net.node.MarkerNode;
import org.junit.jupiter.api.Test;

public class NetRunnerTest {

	@Test
	public void testNetRunner() throws Exception {
		String netStr = "((<eval1>, (<\\==|>, [#])), A=(B=(<\\==|>, (A, [B])), [#]))";
		NetParser parser = new NetParser();
		MarkerNode marker = new MarkerNode();
		parser.permitMarkerNodes(marker);
		Net net = parser.parse(netStr);

		NetFormatter formatter = new NetFormatter();
		formatter.setMarkerNodesPermitted(true);
		formatter.setAscii(true);
		String out0 = formatter.format(net);
		assertThat(out0).isEqualTo(netStr);

		NetController controller = new DefaultNetController(net);
		NetRunner runner = new NetRunner(net, controller);

		StepResult r1 = runner.step();
		assertThat(r1).isEqualTo(StepResult.SUCCESS);

		net.referentialGarbageCollection();

		String out1 = formatter.format(net);
		assertThat(out1).isEqualTo("A=(B=(<\\==|>, (A, [B])), [#])");

		StepResult r2 = runner.step();
		assertThat(r2).isEqualTo(StepResult.SUCCESS);

		net.referentialGarbageCollection();

		String out2 = formatter.format(net);
		assertThat(out2).isEqualTo("a=[B=(<\\==|>, ((B, a), a))]");

		StepResult r3 = runner.step();
		assertThat(r3).isEqualTo(StepResult.FATAL);
	}

}
