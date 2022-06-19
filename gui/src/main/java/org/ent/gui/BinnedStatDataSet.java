package org.ent.gui;

import org.ent.dev.stat.BinnedStat;
import org.jfree.data.DomainOrder;
import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.jfree.data.xy.TableXYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

public class BinnedStatDataSet extends AbstractIntervalXYDataset implements TableXYDataset {

	private static final Logger log = LoggerFactory.getLogger(BinnedStatDataSet.class);

	@Serial
	private static final long serialVersionUID = 1L;

	private final List<BinnedStat> stats;

	private List<String> seriesKeys;

	private int numBinsDisplayed = 80;

	public BinnedStatDataSet(BinnedStat stat) {
		this.stats = new ArrayList<>();
		this.stats.add(stat);
	}

	public BinnedStatDataSet(List<BinnedStat> stats) {
		this.stats = stats;
	}

	public void setSeriesKeys(List<String> seriesKeys) {
		this.seriesKeys = seriesKeys;
	}

	@Override
	public Double getStartX(int series, int item) {
		int bin = getBin(series, item);
		return (double) bin * stats.get(series).getBinSize();
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
		return Math.min(stats.get(series).getNoBins(), numBinsDisplayed + 1);
	}

	@Override
	public int getItemCount() {
		return getItemCount(0);
	}

	@Override
	public Double getX(int series, int item) {
		return (getStartX(series, item) + getEndX(series, item)) / 2;
	}

	private int getBin(int series, int item) {
		int size = stats.get(series).getNoBins();
		if (size <= numBinsDisplayed) {
			return item;
		} else {
			return size - (numBinsDisplayed + 1) + item;
		}
	}

	public int getFirstItemDisplayed() {
		int size = stats.get(0).getNoBins();
		if (size <= numBinsDisplayed) {
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	public Number getY(int series, int item) {
		int bin = getBin(series, item);
		if (bin >= stats.get(series).getNoBins()) {
			log.trace("getY(item={}) -> bin={}, out of bounds", item, bin);
			return null;
		}
		double result = stats.get(series).getValue(bin);
		log.trace("getY(item={}) -> bin={}, value={}", item, bin, result);
		return result;
	}

	@Override
	public int getSeriesCount() {
		return stats.size();
	}

	@Override
	public Comparable getSeriesKey(int series) {
		return seriesKeys != null ? seriesKeys.get(series) : series;
	}

	@Override
	public DomainOrder getDomainOrder() {
		return DomainOrder.ASCENDING;
	}

}
