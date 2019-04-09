package org.ent.dev;

import java.util.Random;

public class DevelopmentPlan {

	private static final long RANDOM_MASTER_SEED = 0xfa1afeL;

	private Random randMaster;

	public static void main(String[] args) throws Exception {
		DevelopmentPlan plan = new DevelopmentPlan();
		plan.execute();
	}

	public void execute() {
		initialize();

		Level0 level0 = new Level0(newRandom());
		for (int i = 1; i <= 100; i++) {
			level0.next();
		}
	}

	private void initialize() {
		randMaster = new Random(RANDOM_MASTER_SEED);
	}

	private Random newRandom() {
		return new Random(randMaster.nextLong());
	}

}
