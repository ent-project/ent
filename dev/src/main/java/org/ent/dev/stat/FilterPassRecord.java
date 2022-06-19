package org.ent.dev.stat;

import org.ent.dev.unit.data.Data;
import org.ent.dev.unit.local.util.FilterListener;

public class FilterPassRecord implements FilterListener {

	private final BinaryStat stats;

	public FilterPassRecord(BinaryStat stats) {
		this.stats = stats;
	}

	@Override
	public void success(Data data) {
		synchronized (stats) {
			stats.addHit();
		}
	}

	@Override
	public void failure(Data data) {
		synchronized (stats) {
			stats.addMiss();
		}
	}
}