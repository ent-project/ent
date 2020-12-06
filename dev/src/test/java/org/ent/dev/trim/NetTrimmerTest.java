package org.ent.dev.trim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

import java.util.stream.Stream;

import org.ent.dev.DefaultTestRunSetup;
import org.ent.dev.RunSetup;
import org.ent.net.Net;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.io.parser.NetParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NetTrimmerTest {

	@ParameterizedTest(name = "{index} => runTrimmer(...) should return {1}")
	@MethodSource("runTrimmer_testData")
	void runTrimmer(String original, String expectedTrimmed) throws Exception {
		NetParser parser = new NetParser();
		Net net = parser.parse(original);

		NetTrimmer trimmer = new NetTrimmer(net, DefaultTestRunSetup.RUN_SETUP);
		trimmer.runTrimmer();

		NetFormatter formatter = new NetFormatter()
				.withNodeNamesInverse(parser.getNodeNames())
				.withAscii(true);
		String actualTrimmed = formatter.format(net);

		assertThat(actualTrimmed).isEqualTo(expectedTrimmed);
	}

	private static Stream<Arguments> runTrimmer_testData() {
	    return Stream.of(
			of(	"(A=(<nop>, [B=(A, [A])]), B)",
				"(A=(<nop>, [#]), (A, [#]))"),

	    	of( "A=((<ix>, A), a=[[[[a]]]])",
		    		"A=((<ix>, A), [#])"),

	    	of( "A=(B=(_a=</dup*>, C=((D=(B, a=[([(((</dup*>, a), [_b=</:\\>]), _a)], _b)]), C), _a)), D); (A, _a); (_a, _a); [D]; [a]",
				"(B=(_a=</dup*>, ((#, #), _a)), (B, [#]))")
	    );
	}


}
