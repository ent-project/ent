package org.ent.dev;

import org.ent.dev.hyper.FloatHyperparameter;
import org.ent.dev.hyper.HyperRegistry;
import org.ent.dev.hyper.IntegerHyperparameter;
import org.ent.dev.plan.Counter;
import org.ent.dev.plan.DataProperties.PropNet;
import org.ent.dev.plan.DataProperties.PropReplicator;
import org.ent.dev.plan.DataProperties.PropSerialNumber;
import org.ent.dev.plan.DataProperties.PropStepsExamResult;
import org.ent.dev.plan.FailReasonRecorder;
import org.ent.dev.plan.Pool;
import org.ent.dev.plan.RandomNetSource;
import org.ent.dev.plan.StepsExam;
import org.ent.dev.plan.StepsExamData;
import org.ent.dev.plan.StepsExamResult;
import org.ent.dev.plan.StepsFilter;
import org.ent.dev.plan.Trimmer;
import org.ent.dev.stat.BinaryStat;
import org.ent.dev.stat.FilterPassRecord;
import org.ent.dev.stat.HtmlColor;
import org.ent.dev.stat.LongStat;
import org.ent.dev.stat.MovingAverage;
import org.ent.dev.stat.PlotInfo;
import org.ent.dev.stat.PlotRegistry;
import org.ent.dev.unit.DeliveryStash;
import org.ent.dev.unit.Req;
import org.ent.dev.unit.SkewSplitter;
import org.ent.dev.unit.Sup;
import org.ent.dev.unit.data.Data;
import org.ent.dev.unit.data.DataProxy;
import org.ent.dev.unit.local.util.FilterListener;
import org.ent.dev.unit.local.TypedProc;
import org.ent.net.Net;
import org.ent.net.io.formatter.NetFormatter;
import org.ent.run.StepResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.Queue;
import java.util.Random;

public class DevelopmentPlan {

	private static final Logger log = LoggerFactory.getLogger(DevelopmentPlan.class);

	private static final long RANDOM_MASTER_SEED = 0xfa1afeL;

	public static final int BATCH_EXECUTION_SIZE_INFINITY = -1;

	private final HyperRegistry hyperRegistry;

	private final Random randMaster;

	private final Output output;

	private final Poller poller;

	private volatile boolean stopped;

	private int level2total;
	private int level2heavyLaneTotal;
	private int level2heavyLanePasses;

	private final BinaryStat level1PassingStat;
	private final EnumMap<StepResult, BinaryStat> level1FailReasonStat;
	private final EnumMap<MixerOutcome, BinaryStat> mixerOutcomeStat;
	private final BinaryStat level2DirectPassesStat;
	private final StopwatchStat stopwatchStat;

	private final NewRandomDrawStat newRandomDrawStat;

	private RoundListener roundListener;

	enum MixerOutcome { WORSE, SAME, BETTER }

	public interface RoundListener {
		void roundCompleted(Data data);
	}

	public static class Poller implements Req {

		private Sup upstream;

		private final Queue<Data> queue = new ArrayDeque<>();

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

	public static class Output extends TypedProc<OutputData> {

		String prefix;

		public Output(String prefix) {
			super(OutputData.class);
			this.prefix = prefix;
		}

		@Override
		public void doAccept(OutputData input) {
			Net net = input.getNet();
			StepsExamResult stepsExamResult = input.getStepsExamResult();
			if (log.isTraceEnabled()) {
				log.trace("{}#{} [{}] {}", prefix, input.getSerialNumber(), stepsExamResult.steps(), new NetFormatter().format(net));
			}
		}

	}

	public static class OutputData extends DataProxy implements PropNet, PropStepsExamResult, PropSerialNumber{}

	public static class AddCopyReplicator extends TypedProc<AddCopyReplicatorData> {

		public AddCopyReplicator() {
			super(AddCopyReplicatorData.class);
		}

		@Override
		public void doAccept(AddCopyReplicatorData input) {
			Net net = input.getNet();
			CopyReplicator replicator = new CopyReplicator(net);
			input.setReplicator(replicator);
		}

	}

	public static class AddCopyReplicatorData extends DataProxy implements PropNet, PropReplicator{}

	static class FailuresLimit implements FilterListener {

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

	private static class StopwatchStat extends LongStat {

		private Long lastTime;

		public StopwatchStat(int binSize) {
			super(binSize);
		}

		private long getCurrentTime() {
			return System.currentTimeMillis();
		}

		public void newRound() {
			long now = getCurrentTime();
			long delta = now - lastTime;
			lastTime = now;
			putValue(delta);
		}

		public void start() {
			lastTime = getCurrentTime();
		}

		public void stop() {
			lastTime = null;
		}
	}

	private static class NewRandomDrawStat extends LongStat {

		private long newRandomDraws;

		public NewRandomDrawStat(int binSize) {
			super(binSize);
		}

		public void newRound() {
			putValue(newRandomDraws);
			newRandomDraws = 0;
		}

		public void recordNewRandomDraw() {
			newRandomDraws++;
		}
	}

	public DevelopmentPlan(PlotRegistry plotRegistry, HyperRegistry hyperRegistry) {
		this.hyperRegistry = hyperRegistry;
		this.randMaster = new Random(RANDOM_MASTER_SEED);
		this.level1PassingStat = new BinaryStat(10_000);
		this.level1FailReasonStat = new EnumMap<>(StepResult.class);
		for (StepResult sr : StepResult.values()) {
			level1FailReasonStat.put(sr, new BinaryStat(10_000));
		}
		this.mixerOutcomeStat = new EnumMap<>(MixerOutcome.class);
		for (MixerOutcome oc : MixerOutcome.values()) {
			mixerOutcomeStat.put(oc, new BinaryStat(1000));
		}
		this.level2DirectPassesStat = new BinaryStat(100);
		this.stopwatchStat = new StopwatchStat(10);
		this.newRandomDrawStat = new NewRandomDrawStat(10);
		this.output = new Output("Result: ");
		this.poller = buildPoller();
		if (plotRegistry != null) {
			plotRegistry.addPlot(new PlotInfo("level1-passing")
					.addRow(level1PassingStat)
					.withTitle("Level 1 passes")
					.withRangeAxisLabel("%")
					.withRangeMax(0.1));
			plotRegistry.addPlot(new PlotInfo("level1-failure-types")
					.addRow(row -> row.withStat(level1FailReasonStat.get(StepResult.FATAL)).withLabel("fatal").withColor(Color.RED))
					.addRow(row -> row.withStat(level1FailReasonStat.get(StepResult.INVALID_COMMAND_BRANCH)).withLabel("invalid command branch").withColor(Color.BLUE))
					.addRow(row -> row.withStat(level1FailReasonStat.get(StepResult.INVALID_COMMAND_NODE)).withLabel("invalid command node").withColor(Color.YELLOW))
					.addRow(row -> row.withStat(level1FailReasonStat.get(StepResult.COMMAND_EXECUTION_FAILED)).withLabel("execution failed").withColor(Color.DARK_GRAY))
					.withTitle("Level 1 failure types")
					.withRangeAxisLabel("%")
					.withRangeMax(1.0));
			plotRegistry.addPlot(new PlotInfo("mixer-outcome")
					.addRow(row -> row.withStat(mixerOutcomeStat.get(MixerOutcome.BETTER)).withLabel("better").withColor(HtmlColor.ForestGreen))
					.addRow(row -> row.withStat(mixerOutcomeStat.get(MixerOutcome.WORSE)).withLabel("worse").withColor(HtmlColor.LightCoral))
					.withTitle("Pool mixer outcome")
					.withRangeAxisLabel("%")
					.withRangeMax(0.3));
			plotRegistry.addPlot(new PlotInfo("level2-direct-passes")
					.addRow(level2DirectPassesStat)
					.withTitle("Direct passes for level 2")
					.withRangeAxisLabel("%")
					.withRangeMax(0.1));
			plotRegistry.addPlot(new PlotInfo("level2-direc-passes-moving-average")
					.addRow(new MovingAverage(level2DirectPassesStat, 20))
					.withSubplotOf("level2-direct-passes"));
			plotRegistry.addPlot(new PlotInfo("stopwatch")
					.addRow(row -> row.withStat(stopwatchStat).withColor(Color.BLUE))
					.withTitle("Execution time for top level events")
					.withRangeAxisLabel("ms")
					.withRangeMax(1000.));
			plotRegistry.addPlot(new PlotInfo("stopwatch-moving-average")
					.withSubplotOf("stopwatch")
					.addRow(row -> row.withStat(new MovingAverage(stopwatchStat, 30)).withColor(Color.BLACK))
			);
			plotRegistry.addPlot(new PlotInfo("new-random-draws")
					.addRow(row -> row.withStat(newRandomDrawStat).withColor(HtmlColor.Brown))
					.withTitle("New random draw per top level event")
					.withRangeAxisLabel("count")
					.withRangeMax(15000.));
		}
	}

	public RoundListener getRoundListener() {
		return roundListener;
	}

	public void setRoundListener(RoundListener roundListener) {
		this.roundListener = roundListener;
	}

	public static void main(String[] args) {
		DevelopmentPlan plan = new DevelopmentPlan(null, null);
		long start = System.currentTimeMillis();
		plan.executeBatch(100);
		long diff = System.currentTimeMillis() - start;
		log.info("execution time: %.3f s%n".formatted(((double) diff) / 1000));
	}

	public void executeBatch(int batchSize) {
		stopped = false;
		stopwatchStat.start();
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
		stopwatchStat.stop();
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
		long level0total = level1PassingStat.getNoEvents();
		long level1total = level1PassingStat.getTotalHits();
		long level2directPasses = level2DirectPassesStat.getTotalHits();

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

		Pool pool;

		Poller result = randomNetSource.toSup()
		.combineProc(data -> newRandomDrawStat.recordNewRandomDraw())
		.combineProc(new StepsExam(getRunSetup()))
		.combineFilter(new StepsFilter(1)
				.with(new FailuresLimit(100000))
				.with(new FilterPassRecord(level1PassingStat))
						.with(new FailReasonRecorder(level1FailReasonStat))
				)
		.combineProc(new Trimmer(getRunSetup()))
		.combineProc(new Counter())
		.combineProc(new Output("level1: "))
		.combineDan(new SkewSplitter()
			.withSorter(new StepsFilter(2)
					.with(new FailuresLimit(100000))
					.with(new FilterPassRecord(level2DirectPassesStat))
					)
			.withLightLane(
				new Output("in light lane: ")
			)
			.withHeavyLane(
				(pool = new Pool(newRandom())).withFeedback(
					new AddCopyReplicator()
					.combineProc(new Counter())
					.combineProc(data -> level2heavyLaneTotal++)
					.combineProc(new StepsExam(getRunSetup()))
					.combineProc(new Output("in heavy lane: "))
					.combineProc(StepsExamData.class, this::recordMixerOutcome)
					.combineFilter(new StepsFilter(2))
					.combineProc(data -> level2heavyLanePasses++)
				)
				.combinePipe(new Output("Passed the heavy lane: "))
				.combinePipe(new Trimmer(getRunSetup()))
				.combinePipe(new Output("trimmed: "))
			)
		)
		.combineProc(data -> {
				level2total++;
				stopwatchStat.newRound();
				newRandomDrawStat.newRound();
			}
		)
		.connectReq(new Poller());

		FloatHyperparameter excludeRatePrimarySuccess = new FloatHyperparameter(1f, "Pool exclude rate: primary success");
		excludeRatePrimarySuccess.setMinimumValue(0f);
		excludeRatePrimarySuccess.setMaximumValue(1f);
		excludeRatePrimarySuccess.addPropertyChangeListener(prop ->
				pool.setExcludeRatePrimarySuccess(excludeRatePrimarySuccess.getValue()));

		FloatHyperparameter excludeRatePrimaryFail = new FloatHyperparameter(1f / 10, "Pool exclude rate: primary failure");
		excludeRatePrimaryFail.setMinimumValue(0f);
		excludeRatePrimaryFail.setMaximumValue(1f);
		excludeRatePrimaryFail.addPropertyChangeListener(prop ->
				pool.setExcludeRatePrimaryFail(excludeRatePrimaryFail.getValue()));

		FloatHyperparameter excludeRateJoiningSuccess = new FloatHyperparameter(1f / 2, "Pool exclude rate: joining success");
		excludeRateJoiningSuccess.setMinimumValue(0f);
		excludeRateJoiningSuccess.setMaximumValue(1f);
		excludeRateJoiningSuccess.addPropertyChangeListener(prop ->
				pool.setExcludeRateJoiningSuccess(excludeRateJoiningSuccess.getValue()));

		FloatHyperparameter excludeRateJoiningFail = new FloatHyperparameter(1f / 50, "Pool exclude rate: joining failure");
		excludeRateJoiningFail.setMinimumValue(0f);
		excludeRateJoiningFail.setMaximumValue(1f);
		excludeRateJoiningFail.addPropertyChangeListener(prop ->
				pool.setExcludeRateJoiningFail(excludeRateJoiningFail.getValue()));

		FloatHyperparameter rewireFraction = new FloatHyperparameter(0.4f, "Pool rewire fraction");
		rewireFraction.setMinimumValue(0f);
		rewireFraction.setMaximumValue(1f);
		rewireFraction.addPropertyChangeListener(prop ->
				pool.setRewireFraction(rewireFraction.getValue()));

		if (hyperRegistry != null) {
			hyperRegistry.addHyperparameter(excludeRatePrimarySuccess);
			hyperRegistry.addHyperparameter(excludeRatePrimaryFail);
			hyperRegistry.addHyperparameter(excludeRateJoiningSuccess);
			hyperRegistry.addHyperparameter(excludeRateJoiningFail);
			hyperRegistry.addHyperparameter(rewireFraction);
		}
		return result;
	}

	private void recordMixerOutcome(StepsExamData data) {
		MixerOutcome outcome = switch (data.getStepsExamResult().steps()) {
			case 0 -> MixerOutcome.WORSE;
			case 1 -> MixerOutcome.SAME;
			default -> MixerOutcome.BETTER;
		};
		for (MixerOutcome oc : MixerOutcome.values()) {
			if (oc == outcome) {
				mixerOutcomeStat.get(oc).addHit();
			} else {
				mixerOutcomeStat.get(oc).addMiss();
			}
		}
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
