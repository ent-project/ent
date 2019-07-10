package org.ent.dev.stat;

public class MovingAverage implements BinnedStats {

	private BinnedStats delegate;

	private int width;

	public MovingAverage(BinnedStats delegate, int width) {
		this.delegate = delegate;
		this.width = width;
	}

	@Override
	public String getTitle() {
		return delegate.getTitle();
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
