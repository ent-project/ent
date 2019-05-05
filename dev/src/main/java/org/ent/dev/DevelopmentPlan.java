package org.ent.dev;

import java.util.Random;

import org.ent.dev.Level0.NetInfoLevel0;
import org.ent.dev.Level1.Level1EventListener;
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

	public void execute() {
		initialize();

		Level0 level0 = new Level0(newRandom());
		Level1 level1 = new Level1(level0);
		StatCounter stat = new StatCounter();
		level1.setLevel1EventListener(stat);
		for (int i = 1; i <= 100; i++) {
			level1.next();
		}
		int total = stat.passes + stat.fails;
		log.info("passed {}/{} ({} %)", stat.passes, total, String.format("%.2f", ((double) stat.passes) / total * 100));
	}

	private void initialize() {
		randMaster = new Random(RANDOM_MASTER_SEED);
	}

	private Random newRandom() {
		return new Random(randMaster.nextLong());
	}

}
