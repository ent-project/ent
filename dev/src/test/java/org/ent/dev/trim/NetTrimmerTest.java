package org.ent.dev.trim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

import java.util.stream.Stream;

import org.ent.dev.RunSetup;
import org.ent.net.Net;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.net.io.parser.NetParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class NetTrimmerTest {

	@ParameterizedTest(name = "{index} => runTrimmer(...) should return {1}")
	@MethodSource("runTrimmer_testData")
	public void runTrimmer(String original, String expectedTrimmed) throws Exception {
		NetParser parser = new NetParser();
		Net net = parser.parse(original);

		RunSetup runSetup = new RunSetup.Builder()
				.withCommandExecutionFailedIsFatal(true)
				.withInvalidCommandBranchIsFatal(true)
				.withInvalidCommandNodeIsFatal(true)
				.withMaxSteps(20)
				.build();

		NetTrimmer trimmer = new NetTrimmer(net, runSetup);
		trimmer.runTrimmer();

		NetFormatter formatter = new NetFormatter();
		formatter.setNodeNamesInverse(parser.getNodeNames());
		formatter.setAscii(true);
		String actualTrimmed = formatter.format(net);

		assertThat(actualTrimmed).isEqualTo(expectedTrimmed);


	}

	@SuppressWarnings("unused")
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
