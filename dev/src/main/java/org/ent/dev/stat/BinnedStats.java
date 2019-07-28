package org.ent.dev.stat;

public interface BinnedStats {

	int getBinSize();

	int getNoBins();

	double getValue(int bin);

}