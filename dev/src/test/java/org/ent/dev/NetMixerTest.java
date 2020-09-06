package org.ent.dev;

import static org.assertj.core.api.Assertions.assertThat;

import org.ent.dev.randnet.RandomTestUtil;
import org.ent.net.Net;
import org.ent.net.io.parser.NetParser;
import org.ent.net.node.MarkerNode;
import org.junit.jupiter.api.Test;

class NetMixerTest {

	@Test
	void join() throws Exception {
		MarkerNode marker = new MarkerNode();
		Net net1 = new NetParser().permitMarkerNodes(marker).parse("A=((<ix>, ([#], A)), [#])");
		Net net2 = new NetParser().permitMarkerNodes(marker).parse("((<nop>, [#]), <eval3>)");
		int net1Size = net1.getNodes().size();
		int net2Size = net2.getNodes().size();


		NetMixer mixer = new NetMixer(RandomTestUtil.newRandom(), net1, net2);
		mixer.join();
		Net netJoined = net1;

		assertThat(netJoined.getNodes().size()).isEqualTo(net1Size + net2Size);
		netJoined.forbidMarkerNode();
		netJoined.consistencyTest();
	}

}
