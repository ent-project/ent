package org.ent.dev.stat;

public class MovingAverage implements BinnedStat {

	private final BinnedStat delegate;

	private final int width;

	public MovingAverage(BinnedStat delegate, int width) {
		this.delegate = delegate;
		this.width = width;
	}

	@Override
	public int getBinSize() {
		return delegate.getBinSize();
	}

	@Override
	public int getNoBins() {
		return delegate.getNoBins();
	}

	@Override
	public double getValue(int bin) {
		int minX = Math.max(0, bin - width + 1);
		double value = 0;
		for (int i = minX; i <= bin; i++) {
			value += delegate.getValue(i);
		}
		int numValues = bin - minX + 1;
		value /= numValues;
		return value;
	}

}
