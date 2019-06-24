package org.ent.dev.stat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class BinaryStatsTest {

	@Test
	public void noBinsAndHits_onlyHits() {
		BinaryStats bs = new BinaryStats(4);

		bs.addHit();
		assertThat(bs.getNoBins()).isEqualTo(0);
		bs.addHit();
		assertThat(bs.getNoBins()).isEqualTo(0);
		bs.addHit();
		assertThat(bs.getNoBins()).isEqualTo(0);
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
	public void noBinsAndHits_onlyMisses() {
		BinaryStats bs = new BinaryStats(4);

		bs.addMiss();
		assertThat(bs.getNoBins()).isEqualTo(0);
		bs.addMiss();
		assertThat(bs.getNoBins()).isEqualTo(0);
		bs.addMiss();
		assertThat(bs.getNoBins()).isEqualTo(0);
		bs.addMiss();
		assertThat(bs.getNoBins()).isEqualTo(1);
		assertThat(bs.getHits(0)).isEqualTo(0);

		bs.addMiss();
		assertThat(bs.getNoBins()).isEqualTo(1);
		assertThat(bs.getHits(0)).isEqualTo(0);
		bs.addMiss();
		assertThat(bs.getNoBins()).isEqualTo(1);
		assertThat(bs.getHits(0)).isEqualTo(0);
		bs.addMiss();
		assertThat(bs.getNoBins()).isEqualTo(1);
		assertThat(bs.getHits(0)).isEqualTo(0);
		bs.addMiss();
		assertThat(bs.getNoBins()).isEqualTo(2);
		assertThat(bs.getHits(0)).isEqualTo(0);
		assertThat(bs.getHits(1)).isEqualTo(0);
	}

	@Test
	public void noBinsAndHits_mixed() {
		BinaryStats bs = new BinaryStats(4);

		bs.addMiss();
		assertThat(bs.getNoBins()).isEqualTo(0);
		bs.addHit();
		assertThat(bs.getNoBins()).isEqualTo(0);
		bs.addMiss();
		assertThat(bs.getNoBins()).isEqualTo(0);
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
	public void noEventsAndTotalHits() {
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
