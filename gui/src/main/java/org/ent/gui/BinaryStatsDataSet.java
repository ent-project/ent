package org.ent.gui;

import org.ent.dev.stat.BinaryStats;
import org.jfree.data.DomainOrder;
import org.jfree.data.xy.AbstractIntervalXYDataset;

public class BinaryStatsDataSet extends AbstractIntervalXYDataset {

	private static final long serialVersionUID = 1L;

	BinaryStats stats;

	int numBinsDisplayed = 100;

	public BinaryStatsDataSet(BinaryStats stats) {
		this.stats = stats;
	}

	@Override
	public Double getStartX(int series, int item) {
		return getX(series, item);
	}

	@Override
	public Double getEndX(int series, int item) {
		return getX(series, item + 1);
	}

	@Override
	public Number getStartY(int series, int item) {
		return null;
	}

	@Override
	public Number getEndY(int series, int item) {
		return null;
	}

	@Override
	public int getItemCount(int series) {
		return numBinsDisplayed;
	}

	@Override
	public Double getX(int series, int item) {
		int bin = getBin(item);
		return (double) bin * stats.getBinSize();
	}

	private int getBin(int item) {
		int size = stats.getSize();
		if (size <= numBinsDisplayed) {
			return item;
		} else {
			return size - numBinsDisplayed + item;
		}
	}

	@Override
	public Number getY(int series, int item) {
		int bin = getBin(item);
		if (bin >= stats.getSize()) {
			return null;
		}
		return stats.getFractionOfHits(bin);
	}

	@Override
	public int getSeriesCount() {
		return 1;
	}

	@Override
	public Comparable<Integer> getSeriesKey(int series) {
		return 0;
	}

	@Override
	public DomainOrder getDomainOrder() {
		return DomainOrder.ASCENDING;
	}

}
