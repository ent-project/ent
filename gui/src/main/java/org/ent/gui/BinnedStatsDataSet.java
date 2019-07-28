package org.ent.gui;

import org.ent.dev.stat.BinnedStats;
import org.jfree.data.DomainOrder;
import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BinnedStatsDataSet extends AbstractIntervalXYDataset {

	private static final Logger log = LoggerFactory.getLogger(BinnedStatsDataSet.class);

	private static final long serialVersionUID = 1L;

	BinnedStats stats;

	int numBinsDisplayed = 80;

	public BinnedStatsDataSet(BinnedStats stats) {
		this.stats = stats;
	}

	@Override
	public Double getStartX(int series, int item) {
		int bin = getBin(item);
		return (double) bin * stats.getBinSize();
	}

	@Override
	public Double getEndX(int series, int item) {
		return getStartX(series, item + 1);
	}

	@Override
	public Number getStartY(int series, int item) {
		return null;
	}

	@Override
	public Number getEndY(int series, int item) {
		return null;
	}

	public int getNumBinsDisplayed() {
		return numBinsDisplayed;
	}

	@Override
	public int getItemCount(int series) {
		return Math.min(stats.getNoBins(), numBinsDisplayed + 1);
	}

	@Override
	public Double getX(int series, int item) {
		return (getStartX(series, item) + getEndX(series, item)) / 2;
	}

	private int getBin(int item) {
		int size = stats.getNoBins();
		if (size <= numBinsDisplayed) {
			return item;
		} else {
			return size - (numBinsDisplayed + 1) + item;
		}
	}

	public int getFirstItemDisplayed() {
		int size = stats.getNoBins();
		if (size <= numBinsDisplayed) {
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	public Number getY(int series, int item) {
		int bin = getBin(item);
		if (bin >= stats.getNoBins()) {
			log.trace("getY(item={}) -> bin={}, out of bounds", item, bin);
			return null;
		}
		double result = stats.getValue(bin);
		log.trace("getY(item={}) -> bin={}, value={}", item, bin, result);
		return result;
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
