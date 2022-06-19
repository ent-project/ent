package org.ent.dev.stat;

import java.util.ArrayList;
import java.util.List;

public class LongStat implements BinnedStat {

	private final int binSize;

	private final List<Long> data;

	private int idx;

	private long nextBinAccumulation;

	public LongStat(int binSize) {
		if (binSize <= 0) {
			throw new IllegalArgumentException();
		}
		this.binSize = binSize;
		this.data = new ArrayList<>();
	}

	public void putValue(long value) {
		nextBinAccumulation += value;
		next();
	}

	private void next() {
		idx++;
		if (idx >= binSize) {
			data.add(nextBinAccumulation);
			nextBinAccumulation = 0L;
			idx = 0;
		}
	}

	@Override
	public int getBinSize() {
		return binSize;
	}

	@Override
	public int getNoBins() {
		return data.size();
	}

	@Override
	public double getValue(int bin) {
		return data.get(bin);
	}

}
