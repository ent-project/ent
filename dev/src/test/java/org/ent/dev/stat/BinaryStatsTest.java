package org.ent.dev.stat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BinaryStatsTest {

	@Test
	void noBinsAndHits_onlyHits() {
		BinaryStats bs = new BinaryStats(4);

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
		BinaryStats bs = new BinaryStats(4);

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
		BinaryStats bs = new BinaryStats(4);

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
		BinaryStats bs = new BinaryStats(4);

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
