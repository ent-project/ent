package org.ent.dev;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

import org.ent.dev.plan.Counter;
import org.ent.dev.plan.DataProperties.PropNet;
import org.ent.dev.plan.DataProperties.PropReplicator;
import org.ent.dev.plan.DataProperties.PropSerialNumber;
import org.ent.dev.plan.DataProperties.PropStepsExamResult;
import org.ent.dev.plan.Pool;
import org.ent.dev.plan.RandomNetSource;
import org.ent.dev.plan.StepsExam;
import org.ent.dev.plan.StepsExamResult;
import org.ent.dev.plan.StepsFilter;
import org.ent.dev.plan.Trimmer;
import org.ent.dev.unit.Data;
import org.ent.dev.unit.DataProxy;
import org.ent.dev.unit.DeliveryStash;
import org.ent.dev.unit.FilterWrapper.FilterListener;
import org.ent.dev.unit.Req;
import org.ent.dev.unit.SkewSplitter;
import org.ent.dev.unit.Sup;
import org.ent.dev.unit.TypedProc;
import org.ent.net.Net;
import org.ent.net.io.formatter.NetFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DevelopmentPlan {

	private static final Logger log = LoggerFactory.getLogger(DevelopmentPlan.class);

	private static final long RANDOM_MASTER_SEED = 0xfa1afeL;

	private Random randMaster;

	public static void main(String[] args) throws Exception {
		DevelopmentPlan plan = new DevelopmentPlan();
		long start = System.currentTimeMillis();
		plan.execute();
		long diff = System.currentTimeMillis() - start;
		System.err.println(String.format("execution time: %.3f s", ((double) diff) / 1000));
	}

	public class Poller implements Req {

		private Sup upstream;

		private Queue<Data> queue = new ArrayDeque<>();

		@Override
		public void setUpstream(Sup upstream) {
			this.upstream = upstream;
		}

		public void poll() {
			upstream.requestNext();
		}

		@Override
		public void receiveNext(Data next) {
			queue.add(next);
		}

		public Data getFromQueue() {
			return queue.remove();
		}

		public boolean queueIsEmpty() {
			return queue.isEmpty();
		}
	}

	public class Output extends TypedProc<OutputData> {

		String prefix;

		public Output(String prefix) {
			super(new OutputData());
			this.prefix = prefix;
		}

		@Override
		public void doAccept(OutputData input) {
			Net net = input.getNet();
			StepsExamResult stepsExamResult = input.getStepsExamResult();
			log.trace("{}#{} [{}] {}", prefix, input.getSerialNumber(), stepsExamResult.getSteps(), new NetFormatter().format(net));
		}

	}

	private class OutputData extends DataProxy implements PropNet, PropStepsExamResult, PropSerialNumber{}

	public class AddCopyReplicator extends TypedProc<AddCopyReplicatorData> {

		public AddCopyReplicator() {
			super(new AddCopyReplicatorData());
		}

		@Override
		public void doAccept(AddCopyReplicatorData input) {
			Net net = input.getNet();
			CopyReplicator replicator = new CopyReplicator(net);
			input.setReplicator(replicator);
		}

	}

	private class AddCopyReplicatorData extends DataProxy implements PropNet, PropReplicator{}

	class FailuresLimit implements FilterListener {

		private final int maxConsecutiveFailures;

		private int consecutiveFailures;

		public FailuresLimit(int maxConsecutiveFailures) {
			this.maxConsecutiveFailures = maxConsecutiveFailures;
		}

		@Override
		public void success(Data data) {
			consecutiveFailures = 0;
		}

		@Override
		public void failure(Data data) {
			consecutiveFailures++;
			if (consecutiveFailures > maxConsecutiveFailures) {
				throw new RuntimeException("Maximum number of consecutive failures exceeded (" + maxConsecutiveFailures + ")");
			}
		}
	}

	private int level0total;
	private int level1total;
	private int level2directPasses;
	private int level2total;
	private int level2heavyLaneTotal;
	private int level2heavyLanePasses;

	public void execute() {
		initialize();

		Poller poller = new RandomNetSource(newRandom()).toSup()
		.combineProc(data -> {level0total++;})
		.combineProc(new StepsExam(getRunSetup()))
		.combineFilter(new StepsFilter(1).with(new FailuresLimit(1000)))
		.combineProc(data -> {level1total++;})
		.combineProc(new Trimmer(getRunSetup()))
		.combineProc(new Counter())
		.combineProc(new Output("level1: "))
		.combineDan(new SkewSplitter()
			.withSorter(new StepsFilter(2))
			.withLightLane(
				new Output("in light lane: ")
				.combineProc(data -> {level2directPasses++;})
			)
			.withHeavyLane(
				new Pool(newRandom()).withFeedback(
					new AddCopyReplicator()
					.combineProc(new Counter())
					.combineProc(data -> {level2heavyLaneTotal++;})
					.combineProc(new StepsExam(getRunSetup()))
					.combineProc(new Output("in heavy lane: "))
					.combineFilter(new StepsFilter(2))
					.combineProc(data -> {level2heavyLanePasses++;})
				)
				.combinePipe(new Output("Passed the heavy lane: "))
				.combinePipe(new Trimmer(getRunSetup()))
				.combinePipe(new Output("trimmed: "))
			)
		)
		.combineProc(data -> {level2total++;})
		.connectReq(new Poller());

		Output output = new Output("Result: ");
		for (int i = 1; i <= 100; i++) {
			poller.poll();
			while (DeliveryStash.instance.hasWork()) {
				DeliveryStash.instance.work();
			}
			Data data = poller.getFromQueue();
			output.accept(data);
			System.out.println();
		}

		log.info("Summary:\n---");
		log.info("level1: passed        {}/{} ({} %)", level1total, level0total, String.format("%.2f", ((double) level1total) / level0total * 100));
		log.info("level2 direct passes: {}/{} ({} %)", level2directPasses, level1total,
				String.format("%.2f", ((double) level2directPasses) / level1total * 100));
		log.info("level2 pool lane:     {}/{} ({} %)",
				level2heavyLanePasses, level2heavyLaneTotal,
				String.format("%.2f", ((double) level2heavyLanePasses) / level2heavyLaneTotal * 100));
	}

	private RunSetup getRunSetup() {
		return new RunSetup.Builder()
				.withCommandExecutionFailedIsFatal(true)
				.withInvalidCommandBranchIsFatal(true)
				.withInvalidCommandNodeIsFatal(true)
				.withMaxSteps(6)
				.build();
	}

	private void initialize() {
		randMaster = new Random(RANDOM_MASTER_SEED);
	}

	private Random newRandom() {
		return new Random(randMaster.nextLong());
	}

}
