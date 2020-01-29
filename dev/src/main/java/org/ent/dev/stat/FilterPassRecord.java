package org.ent.dev.stat;

import org.ent.dev.unit.data.Data;
import org.ent.dev.unit.local.FilterWrapper.FilterListener;

public class FilterPassRecord implements FilterListener {

	private final BinaryStats stats;

	public FilterPassRecord(BinaryStats stats) {
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