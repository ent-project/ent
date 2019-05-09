package org.ent.dev.plan;

import java.util.Random;

import org.ent.dev.Level0;
import org.ent.dev.Level0.NetInfoLevel0;
import org.ent.dev.Level1;
import org.ent.dev.Level1.Level1EventListener;
import org.ent.dev.Level1.NetInfoLevel1;
import org.ent.dev.Level2;
import org.ent.dev.Level2.Level2EventListener;
import org.ent.dev.Level2.NetInfoLevel2;
import org.ent.dev.StepsExam.StepsExamResult;
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

	private static class Level1StatCounter implements Level1EventListener {

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

		Level1StatCounter level1Stat = new Level1StatCounter();
		Level2Listener level2Listener = new Level2Listener();

		Supplier<NetInfoLevel2> poller = new Level0(newRandom())
				.connect(new Level1().withLevel1EventListener(level1Stat))
				.connect(new Trimmer<>())
				.connect(new Level2(newRandom()).withEventListener(level2Listener))
				.connect(new Trimmer<>());

		for (int i = 1; i <= 100; i++) {
			NetInfoLevel2 next = poller.next();
			log.trace("Result {}:", i);
			next.log(log);
		}
		int total = level1Stat.passes + level1Stat.fails;
		log.info("Summary:\n---");
		log.info("level1: passed {}/{} ({} %)", level1Stat.passes, total, String.format("%.2f", ((double) level1Stat.passes) / total * 100));
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
