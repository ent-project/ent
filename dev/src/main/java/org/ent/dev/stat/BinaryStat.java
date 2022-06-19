package org.ent.dev.stat;

import java.util.ArrayList;
import java.util.List;

/**
 * Statistics on a stream of binary outcomes (hit or miss).
 *
 * Collects data into bins of a given size.
 */
public class BinaryStat implements BinnedStat {

	private final List<Integer> data;

	private int idx;

	private int currentBinHits;

	private final int binSize;

	public BinaryStat(int binSize) {
		if (binSize <= 0) {
			throw new IllegalArgumentException();
		}
		this.binSize = binSize;
		this.data = new ArrayList<>();
	}

	@Override
	public int getBinSize() {
		return binSize;
	}

	public void addHit() {
		currentBinHits++;
		next();
	}

	public void addMiss() {
		next();
	}

	private void next() {
		idx++;
		if (idx >= binSize) {
			data.add(currentBinHits);
			currentBinHits = 0;
			idx = 0;
		}
	}

	@Override
	public int getNoBins() {
		return data.size();
	}

	public long getNoEvents() {
		return ((long) data.size()) * binSize + idx;
	}

	public int getHits(int bin) {
		return data.get(bin);
	}

	public long getTotalHits() {
		long hits = 0;
		for (Integer value : data) {
			hits += value;
		}
		hits += currentBinHits;
		return hits;
	}

	@Override
	public double getValue(int bin) {
		return getFractionOfHits(bin);
	}

	public double getFractionOfHits(int bin) {
		Integer hits = data.get(bin);
		return fractionOfHits(hits, binSize);
	}

	private static double fractionOfHits(int hits, int total) {
		if (hits < 0) {
			throw new IllegalArgumentException();
		}
		if (total <= 0) {
			throw new IllegalArgumentException();
		}
		if (total < hits) {
			throw new IllegalArgumentException();
		}
		return ((double) hits) / total;
	}
}
