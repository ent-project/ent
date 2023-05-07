package org.ent.dev;

import org.ent.dev.randnet.RandomTestUtil;
import org.ent.net.Net;
import org.ent.net.io.parser.NetParser;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NetMixerTest {

	@Test
	void join() throws Exception {
		Net net1 = new NetParser().permitMarkerNodes().parse("A:((<x>, ([@], A)), [@])");
		Net net2 = new NetParser().permitMarkerNodes().parse("((<o>, [@]), <=>)");
		int net1Size = net1.getNodes().size();
		int net2Size = net2.getNodes().size();


		NetMixer mixer = new NetMixer(RandomTestUtil.newRandom(), net1, net2);
		mixer.join();
		Net netJoined = net1;

		assertThat(netJoined.getNodes()).hasSize(net1Size + net2Size);
		netJoined.forbidMarkerNode();
		netJoined.consistencyCheck();
	}

}
