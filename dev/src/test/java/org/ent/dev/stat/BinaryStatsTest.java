package org.ent.dev.stat;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BinaryStatsTest {

	@Test
	void noBinsAndHits_onlyHits() {
		BinaryStat bs = new BinaryStat(4);

		bs.addHit();
		assertThat(bs.getNoBins()).isZero();
		bs.addHit();
		assertThat(bs.getNoBins()).isZero();
		bs.addHit();
		assertThat(bs.getNoBins()).isZero();
		bs.addHit();
		assertThat(bs.getNoBins()).isEqualTo(1);
		assertThat(bs.getHits(0)).isEqualTo(4);

		bs.addHit();
		assertThat(bs.getNoBins()).isEqualTo(1);
		assertThat(bs.getHits(0)).isEqualTo(4);
		bs.addHit();
		assertThat(bs.getNoBins()).isEqualTo(1);
		assertThat(bs.getHits(0)).isEqualTo(4);
		bs.addHit();
		assertThat(bs.getNoBins()).isEqualTo(1);
		assertThat(bs.getHits(0)).isEqualTo(4);
		bs.addHit();
		assertThat(bs.getNoBins()).isEqualTo(2);
		assertThat(bs.getHits(0)).isEqualTo(4);
		assertThat(bs.getHits(1)).isEqualTo(4);
	}

	@Test
	void noBinsAndHits_onlyMisses() {
		BinaryStat bs = new BinaryStat(4);

		bs.addMiss();
		assertThat(bs.getNoBins()).isZero();
		bs.addMiss();
		assertThat(bs.getNoBins()).isZero();
		bs.addMiss();
		assertThat(bs.getNoBins()).isZero();
		bs.addMiss();
		assertThat(bs.getNoBins()).isEqualTo(1);
		assertThat(bs.getHits(0)).isZero();

		bs.addMiss();
		assertThat(bs.getNoBins()).isEqualTo(1);
		assertThat(bs.getHits(0)).isZero();
		bs.addMiss();
		assertThat(bs.getNoBins()).isEqualTo(1);
		assertThat(bs.getHits(0)).isZero();
		bs.addMiss();
		assertThat(bs.getNoBins()).isEqualTo(1);
		assertThat(bs.getHits(0)).isZero();
		bs.addMiss();
		assertThat(bs.getNoBins()).isEqualTo(2);
		assertThat(bs.getHits(0)).isZero();
		assertThat(bs.getHits(1)).isZero();
	}

	@Test
	void noBinsAndHits_mixed() {
		BinaryStat bs = new BinaryStat(4);

		bs.addMiss();
		assertThat(bs.getNoBins()).isZero();
		bs.addHit();
		assertThat(bs.getNoBins()).isZero();
		bs.addMiss();
		assertThat(bs.getNoBins()).isZero();
		bs.addHit();
		assertThat(bs.getNoBins()).isEqualTo(1);
		assertThat(bs.getHits(0)).isEqualTo(2);

		bs.addHit();
		assertThat(bs.getNoBins()).isEqualTo(1);
		assertThat(bs.getHits(0)).isEqualTo(2);
		bs.addHit();
		assertThat(bs.getNoBins()).isEqualTo(1);
		assertThat(bs.getHits(0)).isEqualTo(2);
		bs.addHit();
		assertThat(bs.getNoBins()).isEqualTo(1);
		assertThat(bs.getHits(0)).isEqualTo(2);
		bs.addMiss();
		assertThat(bs.getNoBins()).isEqualTo(2);
		assertThat(bs.getHits(0)).isEqualTo(2);
		assertThat(bs.getHits(1)).isEqualTo(3);
	}

	@Test
	void noEventsAndTotalHits() {
		BinaryStat bs = new BinaryStat(4);

		bs.addHit();
		bs.addMiss();
		bs.addMiss();
		bs.addHit();

		bs.addHit();
		bs.addHit();

		assertThat(bs.getNoEvents()).isEqualTo(6);
		assertThat(bs.getTotalHits()).isEqualTo(4);
	}
}
