package org.ent.dev.stat;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BinaryStats implements BinnedStats {

	private List<Integer> data;

	private int idx;

	private int currentBinHits;

	private final int binSize;

	public BinaryStats(int binSize) {
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
		for (int i = 0; i < data.size(); i++) {
			hits += data.get(i);
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
