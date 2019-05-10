package org.ent.dev.plan;

import org.ent.dev.RunSetup;
import org.ent.dev.plan.Data.PropNet;
import org.ent.dev.trim.NetTrimmer;
import org.ent.net.Net;
import org.ent.net.io.formatter.NetFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Trimmer<T extends PropNet> implements Pipe<T, T> {

	private static final Logger log = LoggerFactory.getLogger(Trimmer.class);

	private static final int STEP_LIMIT = 6;

	private static final boolean COMMAND_EXECUTION_FAILED_IS_FATAL = true;

	private static final boolean INVALID_COMMAND_BRANCH_IS_FATAL = true;

	private static final boolean INVALID_COMMAND_NODE_IS_FATAL = true;

	@Override
	public T process(T netInfo) {
		Net net = netInfo.getNet();
		if (log.isTraceEnabled()) {
			log.trace("before trimming: {}", new NetFormatter().withAscii(true).format(net));
		}
		NetTrimmer trimmer = new NetTrimmer(net, getRunSetup());
		trimmer.runTrimmer();
		if (log.isTraceEnabled()) {
			log.trace("after trimming: {}", new NetFormatter().withAscii(true).format(net));
		}
		return netInfo;
	}

	private RunSetup getRunSetup() {
		return new RunSetup.Builder()
				.withCommandExecutionFailedIsFatal(COMMAND_EXECUTION_FAILED_IS_FATAL)
				.withInvalidCommandBranchIsFatal(INVALID_COMMAND_BRANCH_IS_FATAL)
				.withInvalidCommandNodeIsFatal(INVALID_COMMAND_NODE_IS_FATAL)
				.withMaxSteps(STEP_LIMIT)
				.build();
	}
}
