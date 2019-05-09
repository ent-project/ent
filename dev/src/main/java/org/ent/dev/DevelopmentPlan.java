package org.ent.dev;

import java.util.Random;

import org.ent.dev.Level0.NetInfoLevel0;
import org.ent.dev.Level1.Level1EventListener;
import org.ent.dev.Level1.NetInfoLevel1;
import org.ent.dev.Level2.Level2EventListener;
import org.ent.dev.Level2.NetInfoLevel2;
import org.ent.dev.StepsExam.StepsExamResult;
import org.ent.dev.plan.PipeSupplier;
import org.ent.dev.plan.TrimPipe;
import org.ent.net.Net;
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

	private static class StatCounter implements Level1EventListener {

		int passes;
		int fails;

		@Override
		public void netExam(int steps, boolean passed, NetInfoLevel0 net0) {
			if (passed) {
				passes++;
			} else {
				fails++;
			}
		}

	}

	private static class Level2Listener implements Level2EventListener {

		int noFetchedFromLevel1;
		int noPassedDirectly;
		int noPasses;
		int noFails;

		@Override
		public void fetchFromPreviousLevel(NetInfoLevel1 netInfo) {
			noFetchedFromLevel1++;
			netInfo.log(log, "add to pool: ");
		}

		@Override
		public void removeFromPool(NetInfoLevel1 netInfo) {
			log.trace("remove from pool: #{}", netInfo.getSerialNumber());
		}

		@Override
		public void directPass(NetInfoLevel1 netInfo) {
			noPassedDirectly++;
			log.trace("DIRECT PASS: #{}", netInfo.getSerialNumber());
		}

		@Override
		public void examPassed(StepsExamResult result, Net candidate, NetInfoLevel1 netInfoPrimary,
				NetInfoLevel1 netInfoJoining) {
			noPasses++;
			log.trace("EXAM PASSED: #{}+#{}", netInfoPrimary.getSerialNumber(), netInfoJoining.getSerialNumber());
		}

		@Override
		public void examFailed(StepsExamResult result, Net candidate, NetInfoLevel1 netInfoPrimary,
				NetInfoLevel1 netInfoJoining) {
			noFails++;
			log.trace("exam failed: #{}+#{}", netInfoPrimary.getSerialNumber(), netInfoJoining.getSerialNumber());
		}

	}

	public void execute() {
		initialize();

		Level0 level0 = new Level0(newRandom());
		Level1 level1 = new Level1(level0);
		TrimPipe<NetInfoLevel1> trimPipe = new TrimPipe<>();
		PipeSupplier<NetInfoLevel1, NetInfoLevel1> trimPipeSupplier = new PipeSupplier<>(level1, trimPipe);
		Level2 level2 = new Level2(trimPipeSupplier, newRandom());
		Level2Listener level2Listener = new Level2Listener();
		level2.setEventListener(level2Listener);
		TrimPipe<NetInfoLevel2> trimPipe2 = new TrimPipe<>();
		PipeSupplier<NetInfoLevel2, NetInfoLevel2> trimPipeSupplier2 = new PipeSupplier<>(level2, trimPipe2);


		StatCounter stat = new StatCounter();
		level1.setLevel1EventListener(stat);
		for (int i = 1; i <= 10; i++) {
			NetInfoLevel2 next = trimPipeSupplier2.next();
			log.trace("Result {}:", i);
			next.log(log);
		}
		int total = stat.passes + stat.fails;
		log.info("Summary:\n---");
		log.info("level1: passed {}/{} ({} %)", stat.passes, total, String.format("%.2f", ((double) stat.passes) / total * 100));
		int total2 = level2Listener.noPasses + level2Listener.noFails;
		log.info("level2: passed {}/{} ({} %)", level2Listener.noPasses, total2,
				String.format("%.2f", ((double) level2Listener.noPasses) / total2 * 100));
		log.info("level2: passed directly: {}/{} ({} %)",
				level2Listener.noPassedDirectly, level2Listener.noFetchedFromLevel1,
				String.format("%.2f", ((double) level2Listener.noPassedDirectly) / level2Listener.noFetchedFromLevel1 * 100));
	}

	private void initialize() {
		randMaster = new Random(RANDOM_MASTER_SEED);
	}

	private Random newRandom() {
		return new Random(randMaster.nextLong());
	}

}
