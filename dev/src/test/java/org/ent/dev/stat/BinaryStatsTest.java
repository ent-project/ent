package org.ent.dev.stat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class BinaryStatsTest {

	@Test
	public void onlyHits() {
		BinaryStats bs = new BinaryStats(4);

		bs.addHit();
		assertThat(bs.getSize()).isEqualTo(0);
		bs.addHit();
		assertThat(bs.getSize()).isEqualTo(0);
		bs.addHit();
		assertThat(bs.getSize()).isEqualTo(0);
		bs.addHit();
		assertThat(bs.getSize()).isEqualTo(1);
		assertThat(bs.getHits(0)).isEqualTo(4);

		bs.addHit();
		assertThat(bs.getSize()).isEqualTo(1);
		assertThat(bs.getHits(0)).isEqualTo(4);
		bs.addHit();
		assertThat(bs.getSize()).isEqualTo(1);
		assertThat(bs.getHits(0)).isEqualTo(4);
		bs.addHit();
		assertThat(bs.getSize()).isEqualTo(1);
		assertThat(bs.getHits(0)).isEqualTo(4);
		bs.addHit();
		assertThat(bs.getSize()).isEqualTo(2);
		assertThat(bs.getHits(0)).isEqualTo(4);
		assertThat(bs.getHits(1)).isEqualTo(4);
	}

	@Test
	public void onlyMisses() {
		BinaryStats bs = new BinaryStats(4);

		bs.addMiss();
		assertThat(bs.getSize()).isEqualTo(0);
		bs.addMiss();
		assertThat(bs.getSize()).isEqualTo(0);
		bs.addMiss();
		assertThat(bs.getSize()).isEqualTo(0);
		bs.addMiss();
		assertThat(bs.getSize()).isEqualTo(1);
		assertThat(bs.getHits(0)).isEqualTo(0);

		bs.addMiss();
		assertThat(bs.getSize()).isEqualTo(1);
		assertThat(bs.getHits(0)).isEqualTo(0);
		bs.addMiss();
		assertThat(bs.getSize()).isEqualTo(1);
		assertThat(bs.getHits(0)).isEqualTo(0);
		bs.addMiss();
		assertThat(bs.getSize()).isEqualTo(1);
		assertThat(bs.getHits(0)).isEqualTo(0);
		bs.addMiss();
		assertThat(bs.getSize()).isEqualTo(2);
		assertThat(bs.getHits(0)).isEqualTo(0);
		assertThat(bs.getHits(1)).isEqualTo(0);
	}

	@Test
	public void mixed() {
		BinaryStats bs = new BinaryStats(4);

		bs.addMiss();
		assertThat(bs.getSize()).isEqualTo(0);
		bs.addHit();
		assertThat(bs.getSize()).isEqualTo(0);
		bs.addMiss();
		assertThat(bs.getSize()).isEqualTo(0);
		bs.addHit();
		assertThat(bs.getSize()).isEqualTo(1);
		assertThat(bs.getHits(0)).isEqualTo(2);

		bs.addHit();
		assertThat(bs.getSize()).isEqualTo(1);
		assertThat(bs.getHits(0)).isEqualTo(2);
		bs.addHit();
		assertThat(bs.getSize()).isEqualTo(1);
		assertThat(bs.getHits(0)).isEqualTo(2);
		bs.addHit();
		assertThat(bs.getSize()).isEqualTo(1);
		assertThat(bs.getHits(0)).isEqualTo(2);
		bs.addMiss();
		assertThat(bs.getSize()).isEqualTo(2);
		assertThat(bs.getHits(0)).isEqualTo(2);
		assertThat(bs.getHits(1)).isEqualTo(3);
	}

}
