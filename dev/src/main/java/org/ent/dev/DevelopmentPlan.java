package org.ent.dev;

import java.beans.PropertyChangeListener;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

import org.ent.dev.hyper.FloatHyperparameter;
import org.ent.dev.hyper.HyperRegistry;
import org.ent.dev.hyper.IntegerHyperparameter;
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
import org.ent.dev.stat.BinaryStats;
import org.ent.dev.stat.FilterPassRecord;
import org.ent.dev.stat.PlotRegistry;
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

	public static final int BATCH_EXECUTION_SIZE_INFINITY = -1;

	private HyperRegistry hyperRegistry;

	private Random randMaster;

	private Output output;

	private Poller poller;

	private volatile boolean stopped;

	private int level2directPasses;
	private int level2total;
	private int level2heavyLaneTotal;
	private int level2heavyLanePasses;

	private BinaryStats level1PassingStats;

	private RoundListener roundListener;

	public interface RoundListener {
		void roundCompleted(Data data);
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

	public DevelopmentPlan(PlotRegistry plotRegistry, HyperRegistry hyperRegistry) {
		this.hyperRegistry = hyperRegistry;
		this.randMaster = new Random(RANDOM_MASTER_SEED);
		this.level1PassingStats = new BinaryStats(10000);
		this.output = new Output("Result: ");
		this.poller = buildPoller();
		if (plotRegistry != null) {
			plotRegistry.addFractionPlot(level1PassingStats);
		}
	}

	public RoundListener getRoundListener() {
		return roundListener;
	}

	public void setRoundListener(RoundListener roundListener) {
		this.roundListener = roundListener;
	}

	public static void main(String[] args) throws Exception {
		DevelopmentPlan plan = new DevelopmentPlan(null, null);
		long start = System.currentTimeMillis();
		plan.executeBatch(100);
		long diff = System.currentTimeMillis() - start;
		System.err.println(String.format("execution time: %.3f s", ((double) diff) / 1000));
	}

	public void executeBatch(int batchSize) {
		stopped = false;
		if (batchSize == BATCH_EXECUTION_SIZE_INFINITY) {
			while (!stopped) {
				executeOne();
			}
		} else {
			for (int i = 1; i <= batchSize; i++) {
				if (stopped) return;
				executeOne();
			}
		}
		dumpStats();
	}

	private void executeOne() {
		poller.poll();
		while (DeliveryStash.instance.hasWork()) {
			DeliveryStash.instance.work();
		}
		Data data = poller.getFromQueue();
		output.accept(data);
		System.out.println();

		if (roundListener != null) {
			roundListener.roundCompleted(data);
		}
	}

	public void dumpStats() {
		long level0total = level1PassingStats.getNoEvents();
		long level1total = level1PassingStats.getTotalHits();

		log.info("Summary:\n---");
		log.info("level1: passed        {}/{} ({} %)", level1total, level0total, String.format("%.2f", ((double) level1total) / level0total * 100));
		log.info("level2 direct passes: {}/{} ({} %)", level2directPasses, level1total,
				String.format("%.2f", ((double) level2directPasses) / level1total * 100));
		log.info("level2 pool lane:     {}/{} ({} %)",
				level2heavyLanePasses, level2heavyLaneTotal,
				String.format("%.2f", ((double) level2heavyLanePasses) / level2heavyLaneTotal * 100));
	}

	private Poller buildPoller() {

		RandomNetSource randomNetSource = new RandomNetSource(newRandom());

		IntegerHyperparameter noNodesHyper = new IntegerHyperparameter(15, "Number of nodes");
		noNodesHyper.setMinimumValue(0);
		noNodesHyper.setMaximumValue(40);

		FloatHyperparameter fractionCNodes = new FloatHyperparameter(0.2f, "Fraction of C-nodes");
		fractionCNodes.setMinimumValue(0f);
		fractionCNodes.setMaximumValue(1f);

		FloatHyperparameter fractionUNodes = new FloatHyperparameter(0.5f, "Fraction of U-nodes");
		fractionUNodes.setMinimumValue(0f);
		fractionUNodes.setMaximumValue(1f);

		FloatHyperparameter fractionBNodes = new FloatHyperparameter(0.3f, "Fraction of B-nodes");
		fractionBNodes.setMinimumValue(0f);
		fractionBNodes.setMaximumValue(1f);

		noNodesHyper.addPropertyChangeListener(e -> {
			int noNodes = (int) e.getNewValue();
			randomNetSource.setNoNodes(noNodes);
		});
		PropertyChangeListener updateFractions = e -> {
			float valC = fractionCNodes.getValue();
			float valU = fractionUNodes.getValue();
			float valB = fractionBNodes.getValue();
			float sum = valC + valU + valB;
			if (sum == 0) {
				valC = valU = valB = 1f;
				sum = 3f;
			}
			valC /= sum;
			valU /= sum;
			valB /= sum;
			randomNetSource.setFractionCNodes(valC);
			randomNetSource.setFractionUNodes(valU);
			randomNetSource.setFractionBNodes(valB);
		};
		fractionCNodes.addPropertyChangeListener(updateFractions);
		fractionUNodes.addPropertyChangeListener(updateFractions);
		fractionBNodes.addPropertyChangeListener(updateFractions);

		if (hyperRegistry != null) {
			hyperRegistry.addHyperparameter(noNodesHyper);
			hyperRegistry.addHyperparameter(fractionCNodes);
			hyperRegistry.addHyperparameter(fractionUNodes);
			hyperRegistry.addHyperparameter(fractionBNodes);
		}

		return randomNetSource.toSup()
		.combineProc(new StepsExam(getRunSetup()))
		.combineFilter(new StepsFilter(1)
				.with(new FailuresLimit(100000))
				.with(new FilterPassRecord(level1PassingStats))
				)
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
	}

	private RunSetup getRunSetup() {
		return new RunSetup.Builder()
				.withCommandExecutionFailedIsFatal(true)
				.withInvalidCommandBranchIsFatal(true)
				.withInvalidCommandNodeIsFatal(true)
				.withMaxSteps(6)
				.build();
	}

	private Random newRandom() {
		return new Random(randMaster.nextLong());
	}

	public void stop() {
		stopped = true;
	}
}
